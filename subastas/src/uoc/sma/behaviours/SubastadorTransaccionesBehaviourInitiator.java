package uoc.sma.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Comportamiento del Agente buscador implementa FIPA?]Query
 *
 * Se utiliza para enviar la transaccion que debe registrar el agente
 * CasSubastas
 *
 *
 * @author Rodofo de Benito
 */
public class SubastadorTransaccionesBehaviourInitiator extends
		AchieveREInitiator {
	public SubastadorTransaccionesBehaviourInitiator(Agent a, ACLMessage mt) {
		super(a, mt);
	}
}
