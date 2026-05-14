'use strict';

const fs = require('fs');
const path = require('path');

// --- Type Mapping (mirrors SqlTypeMapper.java) ---

function toSqlType(field) {
  // Mermaid erDiagram only allows alphanumeric + underscore in type names (no parentheses or commas).
  // Precision/scale are encoded with underscores: NUMERIC_10_2 means NUMERIC(10,2).
  switch (field.type) {
    case 'String':
      return `varchar_${field.length || 255}`;
    case 'Integer':
      return 'integer';
    case 'Long':
      return 'bigint';
    case 'BigDecimal': {
      if (field.precision != null && field.scale != null) return `numeric_${field.precision}_${field.scale}`;
      if (field.precision != null) return `numeric_${field.precision}`;
      return 'numeric';
    }
    case 'Boolean':
      return 'boolean';
    case 'LocalDate':
      return 'date';
    case 'LocalDateTime':
      return 'timestamp';
    case 'UUID':
      return 'uuid';
    case 'Enum':
      return 'varchar_50';
    default:
      throw new Error(`Unsupported field type: ${field.type}`);
  }
}

// --- Model Parsing ---

function readModels(folderPath) {
  const files = fs.readdirSync(folderPath).filter(f => f.endsWith('.json'));
  return files.map(f => JSON.parse(fs.readFileSync(path.join(folderPath, f), 'utf8')));
}

// --- Entity Block Generation ---

function fieldLine(field) {
  const sqlType = toSqlType(field);
  const key = field.unique ? 'UK' : '';
  const comments = [];
  if (field.nullable) comments.push('nullable');
  if (field.type === 'Enum') comments.push('enum');
  const comment = comments.length > 0 ? ` "${comments.join(' ')}"` : '';
  return `    ${sqlType} ${field.name}${key ? ' ' + key : ''}${comment}`;
}

function entityBlock(model) {
  const lines = [`  ${model.name} {`];
  lines.push('    BIGINT id PK');
  for (const field of (model.fields || [])) {
    lines.push(fieldLine(field));
  }
  for (const rel of (model.relations || [])) {
    if (rel.type === 'ManyToOne') {
      lines.push(`    BIGINT ${rel.joinColumn} FK`);
    }
  }
  lines.push('  }');
  return lines.join('\n');
}

// --- Relationship Edge Generation ---

function relationEdges(models) {
  const edges = [];
  for (const model of models) {
    for (const rel of (model.relations || [])) {
      if (rel.type === 'ManyToOne') {
        edges.push(`  ${model.name} }|--|| ${rel.target} : "${rel.joinColumn}"`);
      }
    }
  }
  return edges;
}

// --- Diagram Assembly ---

function buildDiagram(models) {
  const blocks = models.map(entityBlock).join('\n\n');
  const edges = relationEdges(models).join('\n');

  return [
    '<!-- Auto-generated. Regenerate with: node tools/codegen/er-diagram.js -->',
    '',
    '# Entity-Relationship Diagram',
    '',
    '```mermaid',
    'erDiagram',
    blocks,
    '',
    edges,
    '```',
    '',
  ].join('\n');
}

// --- File Output ---

function writeOutput(content, outputPath) {
  const dir = path.dirname(outputPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  fs.writeFileSync(outputPath, content, 'utf8');
}

// --- Main ---

const DEFAULT_FOLDER = path.join(__dirname, 'examples');
const DEFAULT_OUTPUT = path.join(__dirname, '..', '..', 'docs', 'er-diagram.md');

const folderPath = path.resolve(process.argv[2] || DEFAULT_FOLDER);
const outputPath = path.resolve(process.argv[3] || DEFAULT_OUTPUT);

const models = readModels(folderPath);
const totalRelations = models.reduce((n, m) => n + (m.relations || []).filter(r => r.type === 'ManyToOne').length, 0);

console.log(`Loaded ${models.length} models: ${models.map(m => m.name).join(', ')}`);
console.log(`Relations (ManyToOne): ${totalRelations}`);

const diagram = buildDiagram(models);
writeOutput(diagram, outputPath);

console.log(`ER diagram written to: ${outputPath}`);
