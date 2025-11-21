package p2_tpi.Main;

public class MenuDisplay {

    public static void printHeader() {
        System.out.println("\n========================================");
        System.out.println("      SISTEMA VEHICULAR (Modelo)");
        System.out.println("========================================");
    }

    public static void mainMenu() {
        printHeader();
        System.out.println("1) Vehículos");
        System.out.println("2) Seguros");
        System.out.println("3) Coberturas");
        System.out.println("0) Salir");
        System.out.print("Opción: ");
    }

    public static void vehiculoMenu() {
        System.out.println("\n----- MENÚ VEHÍCULOS -----");
        System.out.println("1) Crear Vehículo + Seguro");
        System.out.println("2) Buscar por dominio");
        System.out.println("3) Listar todos");
        System.out.println("4) Eliminar (soft) Vehículo + Seguro");
        System.out.println("0) Volver");
        System.out.print("Opción: ");
    }

    public static void seguroMenu() {
        System.out.println("\n----- MENÚ SEGUROS -----");
        System.out.println("1) Listar seguros");
        System.out.println("2) Crear seguro");
        System.out.println("3) Actualizar seguro");
        System.out.println("4) Eliminar (soft) seguro");
        System.out.println("5) Buscar por nÃºmero de pÃ³liza");
        System.out.println("0) Volver");
        System.out.print("Opción: ");
    }

    public static void coberturaMenu() {
        System.out.println("\n----- MENÚ COBERTURAS -----");
        System.out.println("1) Listar coberturas");
        System.out.println("2) Crear cobertura");
        System.out.println("3) Actualizar cobertura");
        System.out.println("4) Eliminar (soft) cobertura");
        System.out.println("5) Buscar por cï¿½digo");
        System.out.println("0) Volver");
        System.out.print("Opción: ");
    }
}
