package uoc.sma.behaviours;

import uoc.sma.agentes.AgenteCasaSubastas;
import uoc.sma.datos.Transaccion;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;
import jade.util.Logger;

/**
 * Comportamiento del Agente CasaSubastas que se encarga de atender las
 * peticiones de inserccion de una transaccion en el historial de transacciones
 *
 * Implementa FIPA?]Query con el rol de Participant
 *
 * @author Rodolfo de Benito
 *
 */
@SuppressWarnings("serial")
public class CasaSubastasTransaccionesBehaviourResponder extends
		AchieveREResponder {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());

	public CasaSubastasTransaccionesBehaviourResponder(Agent a,
			MessageTemplate mt) {
		super(a, mt);
		logger.setLevel(Logger.INFO);
	}

	/**
	 * Atiende los mensajes de peticion de registro de una trnasaccion
	 * procedentes de un agente subastador
	 */
	protected ACLMessage handleRequest(ACLMessage request)
			throws NotUnderstoodException, RefuseException {
		Transaccion t = null;
		try {
			t = (Transaccion) request.getContentObject();
			logger.log(Logger.FINE, "Agente " + myAgent.getLocalName()
					+ ": Peticion recibida de " + request.getSender().getName()
					+ ". Transaccion " + t);
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		// Registra la transaccion
		((AgenteCasaSubastas) myAgent).addTransaccion(t);
		logger.log(Logger.FINE, "Agente " + myAgent.getLocalName() + ": Agree");
		ACLMessage agree = request.createReply();
		agree.setPerformative(ACLMessage.AGREE);
		return agree;
	}
}