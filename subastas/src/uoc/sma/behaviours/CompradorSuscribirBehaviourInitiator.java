package uoc.sma.behaviours;

import java.util.Vector;
import uoc.sma.agentes.AgenteComprador;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import jade.util.Logger;

@SuppressWarnings("serial")
/**
 * Clase que modela el comportamiento del comprador como suscriptor
 * de una subasta.
 *
 * Sigue el protocolo FIPA�]Subscription con el rol de Initiator
 *
 */
public class CompradorSuscribirBehaviourInitiator extends SubscriptionInitiator {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());

	public CompradorSuscribirBehaviourInitiator(Agent agente, ACLMessage mensaje) {
		super(agente, mensaje);
		logger.setLevel(Logger.INFO);
	}

	@Override
	/*
	 * Se encarga de crear el mensaje de suscripcion para un subastador
	 * determinado y una subasta concreta
	 */
	protected Vector prepareSubscriptions(ACLMessage subscription) {
		subscription.addReceiver(((AgenteComprador) myAgent).getSubasta()
				.getAgenteSubastador()); // El identificador del subastador
		subscription.setContent(String.valueOf(((AgenteComprador) myAgent)
				.getSubasta().getIdSubasta())); // El id de la subasta a la que
												// se suscribe
		logger.log(Logger.FINE, "Agente " + myAgent.getLocalName()
				+ ": Creando mensaje de suscripcion " + subscription);
		return super.prepareSubscriptions(subscription);
	}

	protected void handleAgree(ACLMessage inform) {
		logger.log(Logger.FINE, myAgent.getLocalName()
				+ ": Solicitud aceptada.");
	}

	protected void handleRefuse(ACLMessage inform) {
		logger.log(Logger.FINE, myAgent.getLocalName()
				+ ": Solicitud rechazada.");
	}

	protected void handleInform(ACLMessage inform) {
		logger.log(Logger.FINE, myAgent.getLocalName() + ": Informe recibido: "
				+ inform.getContent() + "\n");
	}

	protected void handleFailure(ACLMessage failure) {
		// Se comprueba si el fallo viene del AMS o de otro agente.
		if (failure.getSender().equals(myAgent.getAMS())) {
			logger.log(Logger.INFO, myAgent.getLocalName()
					+ ": El agente no existe.");
		} else {
			logger.log(Logger.INFO, myAgent.getLocalName() + ": El agente "
					+ failure.getSender().getName()
					+ " fallo al intentar realizar la accion solicitada.\n");
		}
	}

	public void cancellationCompleted(AID agente) {
		// Se crea una plantilla para solo recibir los mensajes del agente que
		// va a cancelar la suscripcion
		MessageTemplate template = MessageTemplate.MatchSender(agente);
		ACLMessage msg = myAgent.blockingReceive(template);
		// Se comprueba que tipo de mensaje llega: INFORM o FAILURE
		if (msg.getPerformative() == ACLMessage.INFORM)
			logger.log(
					Logger.INFO,
					myAgent.getLocalName()
							+ ": Suscripcion cancelada con el agente "
							+ agente.getLocalName() + "\n");
		else
			logger.log(
					Logger.INFO,
					myAgent.getLocalName()
							+ ": Se ha producido un fallo en la cancelacion con el agente "
							+ agente.getLocalName() + "\n");
	}
}