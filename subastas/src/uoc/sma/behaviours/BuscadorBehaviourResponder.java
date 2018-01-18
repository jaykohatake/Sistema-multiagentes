package uoc.sma.behaviours;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.util.Logger;

/**
 * Comportamiento del Agente buscador que forma parte del protocolo
 * FIPA�]Brokering
 *
 * Envia: �]Mensajes de respuesta AGREE al Agente Comprador cuando recibe una
 * peticion de busqueda
 *
 *
 * @author Rodofo de Benito
 *
 */
public class BuscadorBehaviourResponder extends AchieveREResponder {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());

	public BuscadorBehaviourResponder(Agent a, MessageTemplate mt) {
		super(a, mt);
		logger.setLevel(Logger.INFO);
	}

	protected ACLMessage prepareResponse(ACLMessage request)
			throws NotUnderstoodException, RefuseException {
		logger.log(Logger.FINE, "Agente " + myAgent.getLocalName()
				+ ": Peticion recibida de " + request.getSender().getName()
				+ ". La accion es " + request.getContent());
		ACLMessage agree = request.createReply();
		agree.setPerformative(ACLMessage.AGREE);
		return agree;
	}
}
