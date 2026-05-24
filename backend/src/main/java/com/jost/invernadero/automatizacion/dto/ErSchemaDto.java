package com.jost.invernadero.automatizacion.dto;

import java.util.List;

public record ErSchemaDto(List<EntityDto> entities, List<RelationshipDto> relationships) {}
