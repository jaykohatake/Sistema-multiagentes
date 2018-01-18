package uoc.sma.behaviours.estrategias;

import uoc.sma.agentes.AgenteComprador;
import uoc.sma.estrategias.IncrementalCfgImpl;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.proto.SSIteratedContractNetResponder;
import jade.util.Logger;

/**
 * Estrategia del comprador de la subasta inglesa en la que el agente puja a
 * intervalos regulares incrementando la puja en una cantidad preestablecida al
 * crear el agente. Se puede optar por una de estas dos formas de incrementar la
 * puja:
 *
 * Nueva puja = puja anterior + incremento. Nueva puja = puja anterior *
 * incremento.
 *
 * @author Rodolfo de Benito
 *
 */
@SuppressWarnings("serial")
public class CompradorInglesaIncrementalBehaviour extends
		SSIteratedContractNetResponder {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());
	private AgenteComprador agente;
	private IncrementalCfgImpl estrategia;
	private double puja = 0;
	private int contadorPujas = 0;

	public CompradorInglesaIncrementalBehaviour(Agent a, ACLMessage cfp) {
		super(a, cfp);
		logger.setLevel(Logger.INFO);
		agente = (AgenteComprador) a;
		estrategia = (IncrementalCfgImpl) agente.getEstrategia();
	}

	@Override
	/**
	 * Trata los mensajes Call For Proposal procedentes del comprador con la
	 * puja actual de la subasta
	 *
	 * En este m�todo decide si pujar o no y el importe de la puja, enviando un
	 * mensaje de propuesta (si puja) o de rechazo (si no puja)
	 *
	 */
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException,
			FailureException, NotUnderstoodException {
		ACLMessage firstPropose = cfp.createReply();
		puja = obtenerPuja(Double.parseDouble(cfp.getContent()));
		if ((pujaAceptable(puja))
				&& (puja > Double.parseDouble(cfp.getContent()))) {
			// La puja no sobrepasa el precio de reserva del comprador

			// y adem�s es mayor que la puja actual. Se propone una puja
			firstPropose.setPerformative(ACLMessage.PROPOSE);
			logger.log(Logger.INFO, myAgent.getLocalName()
					+ " : ENVIA : PROPOSE : " + puja);
			firstPropose.setContent(String.valueOf(puja));
			estrategia.setPuja(puja);
			++contadorPujas;
		} else {
			// No puja
			firstPropose.setPerformative(ACLMessage.REFUSE);
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : ENVIA : REFUSE : " + cfp.getContent());
		}
		return firstPropose;
	}

	@Override
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose,
			ACLMessage reject) {
		logger.log(Logger.FINE, myAgent.getLocalName() + " : Recibe : REJECT");
		if (reject == null) {
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : Responder : TIMEOUT");
		}
	}

	@Override
	/**
	 * Trata los mensajes de aceptaci�n de propuesta de puja procedentes del
	 * subastador En caso de recibir un mensaje de aceptaci�n indica que la puja
	 * del agente es la ganadora de la subasta y se env�a un mensaje INFORM al
	 * agente subastador confirmando en importe de la puja ganadora
	 */
	protected ACLMessage handleAcceptProposal(ACLMessage cfp,
			ACLMessage propose, ACLMessage accept) throws FailureException {
		logger.log(Logger.FINE, myAgent.getName() + " : Recibe : ACCEPT");
		if (accept != null) {
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			inform.setContent(String.valueOf(estrategia.getPuja()));
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : Env�a : INFORM : " + puja);
			return inform;
		} else {
			ACLMessage refuse = cfp.createReply();
			refuse.setPerformative(ACLMessage.REFUSE);
			refuse.setContent(String.valueOf(estrategia.getPuja()));
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : Env�a : REFUSE");
			return refuse;
		}
	}

	/**
	 * Calcula la puja que ofertar� el comprador
	 *
	 * @return El valor de la puja
	 */
	private double obtenerPuja(Double pujaActual) {
		double incremento = estrategia.getIncremento();
		int tipoIncremento = estrategia.getTipoIncemento();
		double puja = estrategia.getPuja();
		while (puja < pujaActual) {
			if (tipoIncremento == 0) {
				puja = puja + incremento;
			} else {
				puja = Math.round(puja * incremento);
			}
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " Calculando nueva puja:" + puja);
		}
		return puja;
	}

	/**
	 * Comprueba si la puja es aceptable en base a la estrategia del comprador y
	 * su precio de reserva
	 *
	 * @param puja
	 *            El importe de la puja
	 * @return true si la puja es aceptable y fase en caso contrario
	 */
	private boolean pujaAceptable(double puja) {
		return ((puja < estrategia.getReserva()) && ((puja > estrategia
				.getPuja()) || (contadorPujas == 0)));
	}
}
