package uoc.sma.estrategias;

import uoc.sma.behaviours.estrategias.SubastadorHolandesaBehaviour;
import jade.core.behaviours.Behaviour;

/**
 * Encapsula la configuración de la subasta Holandesa
 *
 * @author Rodolfo de Benito Arango
 *
 */
public class SubastaHolandesaImpl implements ICfgSubasta {
	private SubastadorHolandesaBehaviour comportamiento;

	public Behaviour getComportamiento() {
		return comportamiento;
	}

	public void setComportamiento(Behaviour comportamiento) {
		this.comportamiento = (SubastadorHolandesaBehaviour) comportamiento;
	}
}