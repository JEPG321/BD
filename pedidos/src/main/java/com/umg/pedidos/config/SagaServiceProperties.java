package com.umg.pedidos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public class SagaServiceProperties {

    private final Inventario inventario = new Inventario();
    private final Pagos pagos = new Pagos();
    private final Notificaciones notificaciones = new Notificaciones();

    public Inventario getInventario() {
        return inventario;
    }

    public Pagos getPagos() {
        return pagos;
    }

    public Notificaciones getNotificaciones() {
        return notificaciones;
    }

    public static class Inventario {
        private String baseUrl;
        private String reservePath;
        private String releasePath;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getReservePath() {
            return reservePath;
        }

        public void setReservePath(String reservePath) {
            this.reservePath = reservePath;
        }

        public String getReleasePath() {
            return releasePath;
        }

        public void setReleasePath(String releasePath) {
            this.releasePath = releasePath;
        }
    }

    public static class Pagos {
        private String baseUrl;
        private String chargePath;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getChargePath() {
            return chargePath;
        }

        public void setChargePath(String chargePath) {
            this.chargePath = chargePath;
        }
    }

    public static class Notificaciones {
        private String baseUrl;
        private String sendPath;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getSendPath() {
            return sendPath;
        }

        public void setSendPath(String sendPath) {
            this.sendPath = sendPath;
        }
    }
}
