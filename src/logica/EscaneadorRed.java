package logica;

import modelo.ResultadoEscanio;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class EscaneadorRed {

    public interface EscanioListener {
        void onProgreso(ResultadoEscanio resultado, int actual, int total);
        void onFinalizado();
    }

    private boolean cancelado = false;

    public void detener() {
        this.cancelado = true;
    }

    public void escanearRangoAsync(String ipInicio, String ipFin, int timeout, EscanioListener listener) {
        this.cancelado = false;
        new Thread(() -> {
            List<String> ips = generarListaIPs(ipInicio, ipFin);
            int total = ips.size();

            for (int i = 0; i < total; i++) {
                if (cancelado) break;

                String ip = ips.get(i);
                long inicioTiempo = System.currentTimeMillis();
                boolean activo = false;
                String nombreHost = "Desconocido";
                long tiempoRespuesta = 0;

                try {
                    InetAddress address = InetAddress.getByName(ip);
                    activo = address.isReachable(timeout);
                    tiempoRespuesta = System.currentTimeMillis() - inicioTiempo;

                    if (activo) {
                        String host = address.getCanonicalHostName();
                        if (!host.equals(ip)) {
                            nombreHost = host;
                        }
                    }
                } catch (Exception e) {
                    activo = false;
                }

                ResultadoEscanio res = new ResultadoEscanio(ip, nombreHost, activo, activo ? tiempoRespuesta : 0);
                listener.onProgreso(res, i + 1, total);
            }

            listener.onFinalizado();
        }).start();
    }

    public static boolean esIpValida(String ip) {
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip != null && ip.matches(regex);
    }

    public static long ipToLong(String ip) {
        String[] partes = ip.split("\\.");
        long resultado = 0;
        for (int i = 0; i < 4; i++) {
            resultado = (resultado << 8) + Integer.parseInt(partes[i]);
        }
        return resultado;
    }

    public static String longToIp(long ipLong) {
        return ((ipLong >> 24) & 0xFF) + "." +
               ((ipLong >> 16) & 0xFF) + "." +
               ((ipLong >> 8) & 0xFF) + "." +
               (ipLong & 0xFF);
    }

    private List<String> generarListaIPs(String inicio, String fin) {
        List<String> lista = new ArrayList<>();
        long start = ipToLong(inicio);
        long end = ipToLong(fin);

        if (start > end) {
            long temp = start;
            start = end;
            end = temp;
        }

        for (long i = start; i <= end; i++) {
            lista.add(longToIp(i));
        }
        return lista;
    }
}