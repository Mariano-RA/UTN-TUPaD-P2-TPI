package p2_tpi.Service;

import java.util.List;

public interface GenericService<T> {
    void insertar(T entidad) throws Exception;
    void actualizar(T entidad) throws Exception;
    void eliminar(long id) throws Exception;
    T getById(long id) throws Exception;
    List<T> getAll() throws Exception;
}