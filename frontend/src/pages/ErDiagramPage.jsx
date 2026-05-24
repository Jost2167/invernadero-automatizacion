import 'reactflow/dist/style.css'
import { useEffect } from 'react'
import ReactFlow, {
  Background, Controls, MiniMap,
  useEdgesState, useNodesState,
  getBezierPath, BaseEdge, EdgeLabelRenderer, MarkerType,
} from 'reactflow'
import dagre from '@dagrejs/dagre'
import { Alert, Box, CircularProgress, Typography } from '@mui/material'
import { useTranslation } from 'react-i18next'
import { useState } from 'react'
import schemas from 'virtual:er-schemas'

const NODE_WIDTH = 230
const HEADER_HEIGHT = 36
const ROW_HEIGHT = 22

const CARDINALITY = {
  MANY_TO_ONE:  { source: 'N', target: '1' },
  ONE_TO_MANY:  { source: '1', target: 'N' },
  MANY_TO_MANY: { source: 'N', target: 'M' },
  ONE_TO_ONE:   { source: '1', target: '1' },
}

const OWNING_SIDE = new Set(['ManyToOne', 'OneToOne'])

function toCamelCase(snake) {
  const parts = snake.split('_')
  return parts[0] + parts.slice(1).map(p => p[0].toUpperCase() + p.slice(1)).join('')
}

function toConstantCase(camelCase) {
  return camelCase.replace(/(?<=[a-z])(?=[A-Z])/g, '_').toUpperCase()
}

function buildSchemaData(rawSchemas, locale) {
  const entities = []
  const relationships = []

  for (const schema of rawSchemas) {
    const entityName = schema.name
    const i18n = schema.i18n?.[locale] ?? {}
    const displayName = i18n.singular ?? entityName

    const fields = [{ name: 'id', displayName: 'ID', type: 'Long', primaryKey: true, foreignKey: false }]

    for (const f of schema.fields ?? []) {
      const fdisplay = i18n.fields?.[f.name] ?? f.name
      fields.push({ name: f.name, displayName: fdisplay, type: f.type, primaryKey: false, foreignKey: false })
    }

    for (const rel of schema.relations ?? []) {
      if (OWNING_SIDE.has(rel.type)) {
        const joinCol = rel.joinColumn ?? rel.name + '_id'
        const fkName = toCamelCase(joinCol)
        const relLabel = i18n.relations?.[rel.name] != null
          ? i18n.relations[rel.name] + ' ID'
          : fkName
        fields.push({ name: fkName, displayName: relLabel, type: 'Long', primaryKey: false, foreignKey: true })
      }
      relationships.push({ from: entityName, to: rel.target, type: toConstantCase(rel.type), label: rel.name })
    }

    entities.push({ name: entityName, displayName, fields })
  }

  return { entities, relationships }
}

// Replicates React Flow's internal control-point formula (curvature=0.25)
function bezierControl(pos, x1, y1, x2, y2, c = 0.25) {
  if (pos === 'bottom') return [x1, y1 + c * Math.abs(y2 - y1)]
  if (pos === 'top')    return [x1, y1 - c * Math.abs(y2 - y1)]
  if (pos === 'right')  return [x1 + c * Math.abs(x2 - x1), y1]
  if (pos === 'left')   return [x1 - c * Math.abs(x2 - x1), y1]
  return [x1, y1]
}

function cubicBezier(t, x0, y0, x1, y1, x2, y2, x3, y3) {
  const m = 1 - t
  return [
    m*m*m*x0 + 3*m*m*t*x1 + 3*m*t*t*x2 + t*t*t*x3,
    m*m*m*y0 + 3*m*m*t*y1 + 3*m*t*t*y2 + t*t*t*y3,
  ]
}

function CardinalityEdge({
  id, sourceX, sourceY, targetX, targetY,
  sourcePosition, targetPosition,
  data, markerEnd, style,
}) {
  const [edgePath] = getBezierPath({
    sourceX, sourceY, sourcePosition,
    targetX, targetY, targetPosition,
  })

  const [scx, scy] = bezierControl(sourcePosition, sourceX, sourceY, targetX, targetY)
  const [tcx, tcy] = bezierControl(targetPosition, targetX, targetY, sourceX, sourceY)
  const [srcX, srcY] = cubicBezier(0.18, sourceX, sourceY, scx, scy, tcx, tcy, targetX, targetY)
  const [tgtX, tgtY] = cubicBezier(0.82, sourceX, sourceY, scx, scy, tcx, tcy, targetX, targetY)

  const cardStyle = {
    position: 'absolute',
    pointerEvents: 'none',
    fontSize: 13,
    fontWeight: 'bold',
    color: '#1d4ed8',
    background: 'white',
    padding: '0 3px',
    borderRadius: 3,
    lineHeight: 1.2,
  }

  return (
    <>
      <BaseEdge id={id} path={edgePath} markerEnd={markerEnd} style={style} />
      <EdgeLabelRenderer>
        <div
          className="nodrag nopan"
          style={{ ...cardStyle, transform: `translate(-50%, -50%) translate(${srcX}px, ${srcY}px)` }}
        >
          {data?.sourceCard}
        </div>
        <div
          className="nodrag nopan"
          style={{ ...cardStyle, transform: `translate(-50%, -50%) translate(${tgtX}px, ${tgtY}px)` }}
        >
          {data?.targetCard}
        </div>
      </EdgeLabelRenderer>
    </>
  )
}

const edgeTypes = { cardinality: CardinalityEdge }

function EntityNodeLabel({ entity }) {
  return (
    <Box
      sx={{
        border: '2px solid',
        borderColor: 'primary.main',
        borderRadius: 1,
        overflow: 'hidden',
        width: NODE_WIDTH,
        bgcolor: 'background.paper',
        boxShadow: 2,
      }}
    >
      <Box
        sx={{
          bgcolor: 'primary.main',
          color: 'primary.contrastText',
          px: 1.5,
          height: HEADER_HEIGHT,
          display: 'flex',
          alignItems: 'center',
          fontSize: 13,
          fontWeight: 700,
          letterSpacing: 0.3,
        }}
      >
        {entity.displayName || entity.name}
      </Box>

      <Box sx={{ px: 1, py: 0.5 }}>
        {entity.fields.map((field) => (
          <Box
            key={field.name}
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              height: ROW_HEIGHT,
              gap: 1,
              borderBottom: '1px solid',
              borderColor: 'divider',
              '&:last-child': { borderBottom: 'none' },
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, overflow: 'hidden' }}>
              {field.primaryKey && (
                <Box component="span" sx={{ fontSize: 9, fontWeight: 700, px: 0.5, bgcolor: 'warning.main', color: 'warning.contrastText', borderRadius: 0.5, flexShrink: 0 }}>
                  PK
                </Box>
              )}
              {field.foreignKey && (
                <Box component="span" sx={{ fontSize: 9, fontWeight: 700, px: 0.5, bgcolor: 'info.main', color: 'info.contrastText', borderRadius: 0.5, flexShrink: 0 }}>
                  FK
                </Box>
              )}
              <Box
                component="span"
                sx={{
                  fontSize: 11,
                  fontStyle: field.foreignKey ? 'italic' : 'normal',
                  color: field.primaryKey ? 'warning.dark' : field.foreignKey ? 'info.dark' : 'text.primary',
                  fontWeight: field.primaryKey ? 700 : 400,
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                }}
              >
                {field.displayName || field.name}
              </Box>
            </Box>
            <Box component="span" sx={{ fontSize: 10, color: 'text.disabled', flexShrink: 0 }}>
              {field.type}
            </Box>
          </Box>
        ))}
      </Box>
    </Box>
  )
}

function buildLayout(entities, relationships) {
  const g = new dagre.graphlib.Graph()
  g.setGraph({ rankdir: 'TB', nodesep: 70, ranksep: 90 })
  g.setDefaultEdgeLabel(() => ({}))

  entities.forEach((entity) => {
    const nodeHeight = HEADER_HEIGHT + entity.fields.length * ROW_HEIGHT + 12
    g.setNode(entity.name, { width: NODE_WIDTH, height: nodeHeight })
  })

  relationships.forEach((rel) => {
    g.setEdge(rel.from, rel.to)
  })

  dagre.layout(g)

  const nodes = entities.map((entity) => {
    const { x, y, width, height } = g.node(entity.name)
    return {
      id: entity.name,
      position: { x: x - width / 2, y: y - height / 2 },
      data: { label: <EntityNodeLabel entity={entity} />, entity },
      style: { width, padding: 0, border: 'none', background: 'transparent' },
    }
  })

  const edges = relationships.map((rel, i) => {
    const card = CARDINALITY[rel.type] ?? { source: '?', target: '?' }
    return {
      id: `e-${i}`,
      source: rel.from,
      target: rel.to,
      type: 'cardinality',
      data: { sourceCard: card.source, targetCard: card.target, label: rel.label },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#6b7280', width: 16, height: 16 },
      style: { stroke: '#6b7280', strokeWidth: 1.5 },
    }
  })

  return { nodes, edges }
}

export default function ErDiagramPage() {
  const { t, i18n } = useTranslation()
  const [nodes, setNodes, onNodesChange] = useNodesState([])
  const [edges, setEdges, onEdgesChange] = useEdgesState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const language = i18n.resolvedLanguage

  useEffect(() => {
    setLoading(true)
    setError(null)
    try {
      const { entities, relationships } = buildSchemaData(schemas, language ?? 'es')
      const { nodes: n, edges: e } = buildLayout(entities, relationships)
      setNodes(n)
      setEdges(e)
    } catch {
      setError(t('docs.er.loadError', 'No se pudo cargar el esquema ER.'))
    } finally {
      setLoading(false)
    }
  }, [language])

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    return (
      <Box sx={{ maxWidth: 600, mx: 'auto', py: 4, px: 2 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    )
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 64px)', width: '100%' }}>
      <Typography variant="h6" fontWeight={700} sx={{ px: 2, py: 1, flexShrink: 0 }}>
        {t('docs.er.title')}
      </Typography>
      <Box sx={{ flex: 1, minHeight: 0, position: 'relative' }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          edgeTypes={edgeTypes}
          fitView
          fitViewOptions={{ padding: 0.2 }}
          nodesDraggable={true}
          nodesConnectable={false}
          elementsSelectable={true}
        >
          <MiniMap />
          <Controls />
          <Background color="#e5e7eb" gap={16} />
        </ReactFlow>
      </Box>
    </Box>
  )
}
