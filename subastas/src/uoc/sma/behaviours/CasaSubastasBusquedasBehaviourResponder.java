package uoc.sma.behaviours;

import java.io.IOException;
import uoc.sma.agentes.AgenteCasaSubastas;
import uoc.sma.datos.Subasta;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.util.Logger;

/**
 * Comportamiento del Agente CasaSubastas que se encarga de responder a la
 * peticion de busquedas de subastas del Agente Buscador
 *
 * Implementa FIPa�]Query con el rol de Participant
 *
 * @author Rodolfo de Benito
 *
 */
@SuppressWarnings("serial")
public class CasaSubastasBusquedasBehaviourResponder extends AchieveREResponder {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());

	public CasaSubastasBusquedasBehaviourResponder(Agent a, MessageTemplate mt) {
		super(a, mt);
		logger.setLevel(Logger.FINE);
	}

	/**
	 * Prepara el mensaje de respuesta al agente buscador confirmando que ha
	 * recibido la peticion de busqueda
	 */
	protected ACLMessage prepareResponse(ACLMessage request)
			throws NotUnderstoodException, RefuseException {
		logger.log(Logger.FINE, "Agente " + myAgent.getLocalName()
				+ ": Peticion recibida de " + request.getSender().getName()
				+ ". Accion es " + request.getContent());
		logger.log(Logger.FINE, "Agente " + myAgent.getLocalName() + ": Agree");
		ACLMessage agree = request.createReply();
		agree.setPerformative(ACLMessage.AGREE);
		return agree;
	}

	/**
	 * Prepara el mensage de respuesta al agente buscador con el resultado de la
	 * busqueda
	 */
	protected ACLMessage prepareResultNotification(ACLMessage request,
			ACLMessage response) throws FailureException {
		logger.log(Logger.FINE, "Agente " + myAgent.getLocalName()
				+ ": Busqueda realizada satisfactoriamente");
		Subasta s = ((AgenteCasaSubastas) myAgent).getSubasta(request
				.getContent().toUpperCase());

		ACLMessage inform = request.createReply();
		inform.setPerformative(ACLMessage.INFORM);
		try {
			inform.setContentObject(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inform;
	}
}