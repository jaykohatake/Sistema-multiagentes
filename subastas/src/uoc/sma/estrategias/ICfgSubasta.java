package uoc.sma.estrategias;

import jade.core.behaviours.Behaviour;

/**
 * Encapsula la estrategia que seguirá el agente subastador
 *
 * La finalidad de este interface es ser implementado por las clases que
 * representan los tipos de subastas.
 *
 * @author Rodolfo de Benito Arango
 *
 */
public interface ICfgSubasta {
	/**
	 * Establece el comportamiento en el agente subastador que se le pasa como
	 * referencia
	 *
	 * @param agente
	 */
	public void setComportamiento(Behaviour comportamiento);

	/**
	 * Retorna una referencia al comportamiento o estrategia que seguirá el
	 * subastador
	 *
	 * @return
	 */
	public Behaviour getComportamiento();
}