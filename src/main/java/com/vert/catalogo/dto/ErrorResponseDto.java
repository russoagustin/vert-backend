package com.vert.catalogo.dto;

import com.vert.catalogo.exceptions.ErrorCode;

public record ErrorResponseDto<T>(ErrorCode error, T mensaje) {

}
