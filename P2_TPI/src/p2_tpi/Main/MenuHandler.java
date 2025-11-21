package p2_tpi.Main;

import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;
import p2_tpi.Models.Vehiculo;
import p2_tpi.Service.SeguroVehicularImpl;
import p2_tpi.Service.TipoCoberturaImpl;
import p2_tpi.Service.VehiculoImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MenuHandler {

    private final Scanner sc = new Scanner(System.in);

    private final VehiculoImpl vehiculoService = new VehiculoImpl();
    private final TipoCoberturaImpl coberturaService = new TipoCoberturaImpl();
    private final SeguroVehicularImpl seguroService = new SeguroVehicularImpl();

    public void loop() {
        int op;
        do {
            MenuDisplay.mainMenu();
            op = readInt();
            try {
                switch (op) {
                    case 1 ->
                        vehiculoLoop();
                    case 2 ->
                        segurosLoop();
                    case 3 ->
                        coberturaLoop();
                    case 0 ->
                        System.out.println("Saliendo...");
                    default ->
                        System.out.println("Opción inválida.");
                }

            } catch (Exception e) {
                System.out.println("? ERROR: " + e.getMessage());
            }
        } while (op != 0);
    }

    private void vehiculoLoop() {
        int op;
        do {
            MenuDisplay.vehiculoMenu();
            op = readInt();
            try {
                switch (op) {
                    case 1 ->
                        crearVehiculoConSeguro();
                    case 2 ->
                        buscarVehiculoPorDominio();
                    case 3 ->
                        listarVehiculos();
                    case 4 ->
                        eliminarVehiculoYSeguro();
                    case 0 -> {
                        /* volver */ }
                    default ->
                        System.out.println("Opci�n inv�lida.");
                }
            } catch (Exception e) {
                System.out.println("? ERROR: " + e.getMessage());
            }
        } while (op != 0);
    }

    private void crearVehiculoConSeguro() {
        System.out.println("\n--- NUEVO VEH�CULO ---");
        System.out.print("Dominio: ");
        String dominio = sc.nextLine().trim();
        System.out.print("Marca: ");
        String marca = sc.nextLine().trim();
        System.out.print("Modelo: ");
        String modelo = sc.nextLine().trim();
        System.out.print("A�o (enter si no aplica): ");
        Integer anio = readNullableInt();
        System.out.print("Numero chasis (enter si no aplica): ");
        String chasis = emptyToNull(sc.nextLine().trim());

        System.out.println("\n--- SEGURO VEHICULAR ---");
        System.out.print("Aseguradora: ");
        String aseg = sc.nextLine().trim();
        System.out.print("Numero Poliza: ");
        String poliza = sc.nextLine().trim();
        listarCoberturas(); // para que vea IDs
        System.out.print("ID de cobertura: ");
        Long idCob = readLong();
        System.out.print("Vencimiento (AAAA-MM-DD): ");
        LocalDate vto = LocalDate.parse(sc.nextLine().trim());

        TipoCobertura cobertura = new TipoCobertura();
        cobertura.setId(idCob);

        SeguroVehicular s = new SeguroVehicular();
        s.setAseguradora(aseg);
        s.setNroPoliza(poliza);
        s.setTipoCobertura(cobertura);
        s.setVencimiento(vto);

        Vehiculo v = new Vehiculo();
        v.setDominio(dominio);
        v.setMarca(marca);
        v.setModelo(modelo);
        v.setAnio(anio);
        v.setNroChasis(chasis);
        v.setSeguro(s);

        Long id = vehiculoService.crearVehiculoConSeguro(v);
        System.out.println("? Veh�culo creado con id: " + id);
    }

    private void buscarVehiculoPorDominio() {
        System.out.print("\nDominio a buscar: ");
        String dominio = sc.nextLine().trim();
        Vehiculo v = vehiculoService.buscarPorDominio(dominio);
        System.out.println("\n--- Resultado ---");
        System.out.println("ID: " + v.getId());
        System.out.println("Veh�culo: " + v.getMarca() + " " + v.getModelo() + " (" + v.getDominio() + ")");
        System.out.println("Chasis: " + (v.getNroChasis() == null ? "-" : v.getNroChasis()));
        System.out.println("P�liza: " + v.getSeguro().getNroPoliza());
        System.out.println("Aseguradora: " + v.getSeguro().getAseguradora());
        System.out.println("Cobertura: " + v.getSeguro().getTipoCobertura().getCodigo() + " - "
                + v.getSeguro().getTipoCobertura().getNombre());
        System.out.println("Vencimiento: " + v.getSeguro().getVencimiento());
    }

    private void listarVehiculos() {
        List<Vehiculo> lista = vehiculoService.listar();
        System.out.println("\n--- LISTADO DE VEH�CULOS ---");
        if (lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        for (Vehiculo v : lista) {
            System.out.println(v.getId() + ") " + v.getDominio() + " - " + v.getMarca() + " " + v.getModelo()
                    + " | P�liza " + v.getSeguro().getNroPoliza()
                    + " (" + v.getSeguro().getTipoCobertura().getCodigo() + ")");
        }
    }

    private void eliminarVehiculoYSeguro() {
        System.out.print("\nID de veh�culo a eliminar (soft): ");
        Long id = readLong();
        vehiculoService.eliminar(id);
        System.out.println("? Eliminado con �xito (veh�culo + seguro).");
    }

    private void segurosLoop() {
        int op;
        do {
            MenuDisplay.seguroMenu();
            op = readInt();
            try {
                switch (op) {
                    case 1 -> listarSeguros();
                    case 2 -> crearSeguro();
                    case 3 -> actualizarSeguro();
                    case 4 -> eliminarSeguro();
                    case 5 -> buscarSeguroPorPoliza();
                    case 0 -> {
                        System.out.println("Volviendo al menú principal...");
                    }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("? ERROR: " + e.getMessage());
            }
        } while (op != 0);
    }

    private void listarSeguros() {
        List<SeguroVehicular> lista = seguroService.listar(); // asumimos listar() en el service
        System.out.println("\n--- LISTADO DE SEGUROS ---");
        if (lista == null || lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        for (SeguroVehicular s : lista) {
            System.out.println(
                    s.getId() + ") " +
                            s.getAseguradora() + " | Póliza " + s.getNroPoliza() +
                            " | Cobertura: " + (s.getTipoCobertura() != null ? s.getTipoCobertura().getCodigo() : "SIN")
                            +
                            " | Vencimiento: " + s.getVencimiento());
        }
    }

    private void crearSeguro() throws Exception {
    System.out.println("\n--- NUEVO SEGURO ---");

    System.out.print("Aseguradora: ");
    String aseg = sc.nextLine().trim();

    System.out.print("N° póliza (única): ");
    String poliza = sc.nextLine().trim();

    listarCoberturas(); // ya existe este método para mostrar opciones
    System.out.print("ID de cobertura: ");
    Long idCob = readLong();

    System.out.print("Vencimiento (AAAA-MM-DD): ");
    LocalDate vto = LocalDate.parse(sc.nextLine().trim());

    TipoCobertura cobertura = new TipoCobertura();
    cobertura.setId(idCob);

    SeguroVehicular s = new SeguroVehicular();
    s.setAseguradora(aseg);
    s.setNroPoliza(poliza);
    s.setTipoCobertura(cobertura);
    s.setVencimiento(vto);

    Long id = seguroService.crear(s); // usa crear(SeguroVehicular) del service
    System.out.println("? Seguro creado con id: " + id);
}

    private void actualizarSeguro() throws Exception {
    System.out.println("\n--- ACTUALIZAR SEGURO ---");
    System.out.print("Número de póliza a actualizar: ");
    String poliza = sc.nextLine().trim();

    SeguroVehicular s = seguroService.buscarPorPoliza(poliza);
    if (s == null) {
        System.out.println("No se encontró un seguro con esa póliza.");
        return;
    }

    System.out.println("Seguro actual:");
    System.out.println(s);

    System.out.print("Nueva aseguradora (enter para dejar igual): ");
    String nuevaAseg = sc.nextLine().trim();
    if (!nuevaAseg.isEmpty()) {
        s.setAseguradora(nuevaAseg);
    }

    System.out.print("Nuevo número de póliza (enter para dejar igual): ");
    String nuevaPoliza = sc.nextLine().trim();
    if (!nuevaPoliza.isEmpty()) {
        s.setNroPoliza(nuevaPoliza);
    }

    System.out.print("Cambiar cobertura? (S/N): ");
    String resp = sc.nextLine().trim().toUpperCase();
    if (resp.equals("S")) {
        listarCoberturas();
        System.out.print("Nuevo ID de cobertura: ");
        Long idCob = readLong();
        TipoCobertura tc = new TipoCobertura();
        tc.setId(idCob);
        s.setTipoCobertura(tc);
    }

    System.out.print("Nueva fecha de vencimiento (AAAA-MM-DD, enter para dejar igual): ");
    String nuevaFecha = sc.nextLine().trim();
    if (!nuevaFecha.isEmpty()) {
        s.setVencimiento(LocalDate.parse(nuevaFecha));
    }

    int rows = seguroService.actualizar(s);
    System.out.println(rows > 0 ? "? Seguro actualizado." : "? No se actualizó.");
}

    private void eliminarSeguro() throws Exception {
    System.out.println("\n--- ELIMINAR (SOFT) SEGURO ---");
    System.out.print("Número de póliza a eliminar: ");
    String poliza = sc.nextLine().trim();

    SeguroVehicular s = seguroService.buscarPorPoliza(poliza);
    if (s == null) {
        System.out.println("No se encontró un seguro con esa póliza.");
        return;
    }

    System.out.println("Seguro encontrado:");
    System.out.println(s);
    System.out.print("¿Confirmar eliminación lógica? (S/N): ");
    String conf = sc.nextLine().trim().toUpperCase();
    if (!conf.equals("S")) {
        System.out.println("Operación cancelada.");
        return;
    }

    int rows = seguroService.eliminar(s.getId());
    System.out.println(rows > 0 ? "? Seguro eliminado (soft)." : "? No se eliminó.");
}

    private void buscarSeguroPorPoliza() throws Exception {
    System.out.println("\n--- BUSCAR SEGURO POR N° DE PÓLIZA ---");
    System.out.print("Número de póliza: ");
    String poliza = sc.nextLine().trim();

    SeguroVehicular s = seguroService.buscarPorPoliza(poliza);
    if (s == null) {
        System.out.println("No se encontró un seguro con esa póliza.");
        return;
    }

    System.out.println("Seguro encontrado:");
    System.out.println(s);
}

    private void coberturaLoop() {
        int op;
        do {
            MenuDisplay.coberturaMenu();
            op = readInt();
            try {
                switch (op) {
                    case 1 ->
                        listarCoberturas();
                    case 2 ->
                        crearCobertura();
                    case 3 ->
                        actualizarCobertura();
                    case 4 ->
                        eliminarCobertura();
                    case 5 ->
                        buscarCoberturaPorCodigo();
                    default ->
                        System.out.println("Opci�n inv�lida.");
                }
            } catch (Exception e) {
                System.out.println("? ERROR: " + e.getMessage());
            }
        } while (op != 0);
    }

    private void listarCoberturas() {
        List<TipoCobertura> list = coberturaService.listar();
        System.out.println("\n--- TIPOS DE COBERTURA ---");
        if (list.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        for (TipoCobertura c : list) {
            System.out.println(c.getId() + ") " + c.getCodigo() + " - " + c.getNombre());
        }
    }

    private void crearCobertura() {
        System.out.println("\n--- NUEVA COBERTURA ---");
        System.out.print("C�digo (�nico): ");
        String codigo = sc.nextLine().trim();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine().trim();
        System.out.print("Descripci�n (enter si no aplica): ");
        String desc = emptyToNull(sc.nextLine());

        TipoCobertura t = new TipoCobertura();
        t.setCodigo(codigo);
        t.setNombre(nombre);
        t.setEliminado(false);
        t.setOrden(1);

        Long id = coberturaService.crear(t);
        System.out.println("? Cobertura creada con id: " + id);
    }

    private void actualizarCobertura() {
        listarCoberturas();
        System.out.print("\nID de cobertura a actualizar: ");
        Long id = readLong();
        System.out.print("Nuevo c�digo: ");
        String codigo = sc.nextLine().trim();
        System.out.print("Nuevo nombre: ");
        String nombre = sc.nextLine().trim();
        System.out.print("Nueva descripci�n (enter si no aplica): ");
        String desc = emptyToNull(sc.nextLine());

        TipoCobertura t = new TipoCobertura();
        t.setId(id);
        t.setCodigo(codigo);
        t.setNombre(nombre);

        int rows = coberturaService.actualizar(t);
        System.out.println(rows > 0 ? "? Actualizada." : "? No se actualiz�.");
    }

    private void eliminarCobertura() {
        listarCoberturas();
        System.out.print("\nID de cobertura a eliminar (soft): ");
        Long id = readLong();
        int rows = coberturaService.eliminar(id);
        System.out.println(rows > 0 ? "? Eliminada." : "? No se elimin�.");
    }

    private void buscarCoberturaPorCodigo() {
        System.out.print("\nC�digo: ");
        String codigo = sc.nextLine().trim();
        TipoCobertura t = coberturaService.buscarPorCodigo(codigo);
        System.out.println("ID: " + t.getId() + " | " + t.getCodigo() + " - " + t.getNombre());
    }

    private int readInt() {
        try {
            String s = sc.nextLine().trim();
            return Integer.parseInt(s);
        } catch (Exception e) {
            return -1;
        }
    }

    private Long readLong() {
        while (true) {
            try {
                String s = sc.nextLine().trim();
                return Long.parseLong(s);
            } catch (Exception e) {
                System.out.print("N�mero inv�lido, reintente: ");
            }
        }
    }

    private Integer readNullableInt() {
        String s = sc.nextLine().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            System.out.print("N�mero inv�lido, deje vac�o o reintente (entero): ");
            return readNullableInt();
        }
    }

    private String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
