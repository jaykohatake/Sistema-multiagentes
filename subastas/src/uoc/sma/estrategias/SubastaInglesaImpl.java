package uoc.sma.estrategias;

import uoc.sma.behaviours.estrategias.SubastadorInglesaBehaviour;
import jade.core.behaviours.Behaviour;

/**
 * Encapsula la configuración de la subasta Inglesa
 *
 * @author Rodolfo de Benito Arango
 *
 */
public class SubastaInglesaImpl implements ICfgSubasta {
	private SubastadorInglesaBehaviour comportamiento;

	public Behaviour getComportamiento() {
		return comportamiento;
	}

	public void setComportamiento(Behaviour comportamiento) {
		this.comportamiento = (SubastadorInglesaBehaviour) comportamiento;
	}
}
