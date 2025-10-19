package Planificacion;

import model.Proceso;
import Edd.Cola;

/**
 *
 * @author sarazo
 */
public interface AlgoritmoPlanificacion {
    void agregarProceso(Proceso proceso);
    Proceso obtenerSiguienteProceso();
    Cola getColaListos();
    String getNombre();
}
