package Core.Interfaces;

import java.util.List;

public interface IBaseDAO<E> {
    List<E> readAll();
    boolean writeAll(List<E> list);
    boolean add(E item);
    boolean update(E item);
    boolean delete(String id);
    E findByID(String id);
}