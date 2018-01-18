package uoc.sma.agentes;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Logger;

/**
 * Clase base para loa agentes que contiene servicios comunes a todos ellos
 *
 * @author Rodolfo de Benito Arango
 *
 */
@SuppressWarnings("serial")
public class AgenteBase extends Agent {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());

	@Override
	/*
	 * Procedimiento que se ejecuta en la creación del agente
	 */
	protected void setup() {
		// Establece el nivel de mensajes del logger
		logger.setLevel(Logger.INFO);
	}

	/**
	 * Registra a un agente en las páginas amarillas para facilitar su
	 * localización
	 *
	 * @param tipoAgente
	 *            El tipo de agente a registrar
	 * @param nombre
	 *            El nombre del agente
	 *
	 */
	protected void registrarDFAgente(String tipoAgente, String nombre) {
		logger.log(Logger.FINE, this.getLocalName() + " registrando agente "
				+ nombre + " en páginas amarillas");
		// Crea la descripción del agente y le asigna el nombre de agente
		DFAgentDescription descripcionAgente = new DFAgentDescription();
		descripcionAgente.setName(getAID());
		// Crea la descripción del servicio y le asigna el nombre del agente y
		// tipo
		ServiceDescription sd = new ServiceDescription();
		sd.setType(tipoAgente);
		sd.setName(nombre);
		descripcionAgente.addServices(sd);
		try {
			// Registra el agente en las páginas amarillas
			DFService.register(this, descripcionAgente);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Localiza a un agente en las páginas amarillas
	 *
	 * @param tipo
	 *            El tipo de agente a localizar
	 * @return El array de agentes que responden al tpo de agente a localizar
	 */
	protected DFAgentDescription[] getAgentePaginasAmarillas(String tipo) {
		logger.log(Logger.FINE, this.getLocalName() + " localizando agente "
				+ tipo + " en páginas amarillas");
		// Crea la plantilla tipo del agente a localizar
		DFAgentDescription plantilla = new DFAgentDescription();
		DFAgentDescription[] resultado = null;
		ServiceDescription sd = new ServiceDescription();
		sd.setType(tipo);
		plantilla.addServices(sd);
		try {
			// Busca al agente en las páginas amarillas
			resultado = DFService.search(this, plantilla);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return resultado;
	}
}