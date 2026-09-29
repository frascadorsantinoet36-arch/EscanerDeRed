# EscanerDeRed

Un escaneador de red local diseñado para descubrir dispositivos activos, analizar puertos e inspeccionar la topología de la red de manera rápida y eficiente.

---

## Características

- **Descubrimiento de Hosts:** Escaneo de rangos IP en la red local para identificar equipos activos (ping sweep / ARP).
- **Escaneo de Puertos:** Detección de puertos abiertos (TCP/UDP) y servicios en ejecución.
- **Detección de Información:** Identificación de direcciones MAC y nombres de host (Hostname).
- **Interfaz Sencilla:** Reportes claros por consola o interfaz gráfica.

---

## Tecnologías Utilizadas

- **Lenguaje:** [Python / Node.js / C#] *(modificar según el lenguaje usado)*
- **Librerías/Herramientas:** [Nmap / Scapy / Socket / Net-snmp] *(listar las dependencias principales)*

---

## Requisitos Previos

Asegúrate de contar con los siguientes elementos instalados antes de ejecutar el proyecto:

- [Python 3.x / Node.js v18+] *(según corresponda)*
- Permisos de administrador/root (necesarios para el envío de paquetes ICMP/ARP raw en la red).

---

## Instalación

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/TuUsuario/EscanerDeRed.git](https://github.com/TuUsuario/EscanerDeRed.git)
   cd EscanerDeRed
