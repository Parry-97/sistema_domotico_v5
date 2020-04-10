package inge.progetto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Ogni {@link Sensore} &egrave; descritto da una categoria. Essa &egrave; caratterizzata da un {@link #nome}, da un {@link #testolibero} di lunghezza
 * massima predefinita (per esprimere ad esempio la sigla, il costruttore ecc.) e dall'insieme(eventualmente singoletto) delle informazioni
 * rilevabili da ogni sensore di quella categoria. Inoltre viene anche specificata la natura del sensore ({@link #fisico}) ovvero se
 * quest'ultimo pu&ograve; essere utilizzato per la misura di grandezze fisiche(temperatura, pressione) o per il monitoraggio di artefatti
 * presenti all'interno dell'unit&agrave; immobiliare
 *
 * @see Informazione
 * @see Artefatto
 * @author Parampal Singh, Mattia Nodari
 */
public class CategoriaSensore implements Serializable {
    /**
     * nome della categoria di sensori
     */
    private String nome;

    /**
     * scopo/natura o impiego del sensore
     */
    private boolean fisico;

    /**
     * testo libero descrittivo della categoria
     */
    private String testolibero;

    /**
     * l'informazione rilevabile da sensori di questa categoria
     */
    private ArrayList<Informazione> infoRilevabili;

    /**Costruttore per la specifica di un oggetto di tipo CategoriaSensore
     * @param nome nome della categoria
     * @param testolibero testo libero descrittivo
     * @param fisico scopo della categoria di sensore
     */
    public CategoriaSensore(String nome, String testolibero, boolean fisico) {
        this.nome = nome;
        this.testolibero = testolibero;
        this.fisico = fisico;
        // TODO: 10/04/2020 mettere in v3,v4
        infoRilevabili = new ArrayList<>();
    }

    /**Costruttore per la specifica di un oggetto di tipo CategoriaSensore
     * @param nome nome della categoria
     * @param testolibero testo libero descrittivo
     * @param fisico scopo della categoria di sensore
     * @param infos specifica le informazioni descritte dalla categoria di sensore
     */
    public CategoriaSensore(String nome, String testolibero, boolean fisico, ArrayList<Informazione> infos) {
        this.nome = nome;
        this.testolibero = testolibero;
        this.fisico = fisico;
        this.infoRilevabili = infos;
    }

    /**Permette di ottenere il nome della categoria di sensore
     * @return nome della categoria di sensore
     */
    public String getNome() {
        return nome;
    }


    /**Specifica il nome della categoria di sensore
     * @param nome nuovo nome da assegnare alla categoria di sensore
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**Fornisce il testo libero associato alla categoria di sensore
     * @return testo libero relativo alla categoria di sensore
     */
    public String getTestolibero() {
        return testolibero;
    }

    /**Permette di specificare/modificare il testo libero della categoria
     * @param testolibero nuovo testo libero da assegnare alla categoria
     */
    public void setTestolibero(String testolibero) {
        this.testolibero = testolibero;
    }


    /**Fornisce l'informazione rilevabile da sensori di questa categoria
     * @return l'informazione rilevabile da sensori della stessa categoria
     */

    //TODO: Controllare non abbia ripercussioni troppo gravi sul resto del codice ️
    public ArrayList<Informazione> getInfoRilevabili() {
        return this.infoRilevabili;
    }

    //TODO: Aggiungere in v3,v4
    public ArrayList<Informazione> getCopiaInfoRilevabili() {
        ArrayList<Informazione> newInfos = new ArrayList<>();

        for (Informazione info : this.infoRilevabili) {
            try {

                Informazione newInfo = (Informazione) info.clone();
                newInfos.add(newInfo);

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return newInfos;
    }

    /**Permette di specificare il tipo di informazione rilevabile da sensori della stessa categoria
     * @param info nuova informazione rilevabile da assegnare alla categoria di sensori
     */
    public void setInfoRilevabili(ArrayList<Informazione> info) {
        this.infoRilevabili = info;
    }

    /**Permette di conoscere lo scopo/natura dei sensori di una categoria.
     * Tutti i sensori di una stessa categoria possono quindi essere 'fisici', misurano grandezze fisiche, o 'non-fisici', monitorano
     * quindi artefatti.
     * @return tipo di categoria di sensore
     */
    public boolean isFisico() {
        return fisico;
    }

    /**Permette di specificare la natura della categoria di sensori
     * @param fisico valore di flag che definisce la natura 'fisica' dei sensori della categoria
     */
    public void setFisico(boolean fisico) {
        this.fisico = fisico;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoriaSensore that = (CategoriaSensore) o;

        if (isFisico() != that.isFisico()) return false;
        if (!getNome().equals(that.getNome())) return false;
        if (!getTestolibero().equals(that.getTestolibero())) return false;
        return getInfoRilevabili().equals(that.getInfoRilevabili());
    }

    @Override
    public int hashCode() {
        int result = getNome().hashCode();
        result = 31 * result + (isFisico() ? 1 : 0);
        result = 31 * result + getTestolibero().hashCode();
        result = 31 * result + getInfoRilevabili().hashCode();
        return result;
    }

}
