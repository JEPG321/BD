package com.laboratorio.msnotificaciones.dto;

import java.util.List;

public class ErrorResponse {

    private boolean ok;
    private String mensaje;
    private List<String> errores;

    public ErrorResponse() {
    }

    public ErrorResponse(boolean ok, String mensaje, List<String> errores) {
        this.ok = ok;
        this.mensaje = mensaje;
        this.errores = errores;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void setErrores(List<String> errores) {
        this.errores = errores;
    }
}
