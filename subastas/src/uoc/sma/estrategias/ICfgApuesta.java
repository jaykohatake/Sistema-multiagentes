package uoc.sma.estrategias;

import jade.core.behaviours.Behaviour;

/**
 * Encapsula la estrategia que seguirá el agente comprador
 *
 * La finalidad de este interface es ser implementado por las clases que
 * representan las diferentes estrategias que seguirá el agente comprador.
 *
 * @author Rodolfo de Benito Arango
 *
 */
public interface ICfgApuesta {
	/**
	 * Establece el comportamiento en el agente comprador que se le pasa como
	 * referencia
	 *
	 * @param agente
	 */
	public void setComportamiento(Behaviour comportamiento);

	/**
	 * Retorna una referencia al comportamiento o estrategia que seguirá el
	 * comprador
	 *
	 * @return
	 */
	public Behaviour getComportamiento();
}