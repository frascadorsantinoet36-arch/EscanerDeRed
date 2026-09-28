package modelo;

public class ResultadoEscanio {
    private String ip;
    private String nombreEquipo;
    private boolean activo;
    private long tiempoMs;

    public ResultadoEscanio(String ip, String nombreEquipo, boolean activo, long tiempoMs) {
        this.ip = ip;
        this.nombreEquipo = nombreEquipo;
        this.activo = activo;
        this.tiempoMs = tiempoMs;
    }

    public String getIp() { return ip; }
    public String getNombreEquipo() { return nombreEquipo; }
    public boolean isActivo() { return activo; }
    public long getTiempoMs() { return tiempoMs; }
}