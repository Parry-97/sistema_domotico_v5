package inge.progetto;

import java.io.Serializable;

/**
 * Rappresenta l'informazione/misura numerica che il sistema domotico acquisisce attaverso ogni singolo sensori dislocati
 * nelle sottounit&agrave; immobiliari soggette al controllo. Ciascuna informazione &egrave; identificata da un {@link #nome}
 * e pu&ograve; assumere un valore che cade entro un dominio ({@link #VALORE_MIN} , {@link #VALORE_MAX}).
 *
 * @author Parampal Singh, Mattia Nodari
 */
public class Informazione implements Serializable,Cloneable{


    /**
     * nome dell'informazione rilevabile
     */
    private String nome;

    /**
     * Tipo dell'informazione che il sensore acquisisce. N = numerica, NN = non numerica.
     */
    private String tipo;

    /**
     * valore corrente assunto dall'informazione rilevabile
     */
    protected Object valore;

    /**
     * estremo superiore del range/dominio dell'informazione
     */
    private int  VALORE_MAX = Integer.MAX_VALUE;

    /**
     * estremo superiore del range/dominio dell'informazione
     */
    private int VALORE_MIN = 0;


    /**Costruttore per un oggetto di tipo {@link Informazione}
     * @param nome nome da assegnare all'informazione
     */
    public Informazione(String nome) {
        this.nome = nome;
        this.tipo = "N";
        this.valore = 0;
    }

    /** Costruttore per un oggetto di tipo {@link Informazione}
     * @param nome nome da assegnare all'informazione
     * @param valoreMax valore massimo del range/dominio dell'informazione
     * @param valoreMin valore minimo del range/dominio dell'informazione
     */
    public Informazione(String nome, int valoreMax, int valoreMin) {
        this.nome = nome;
        this.VALORE_MAX = valoreMax;
        this.VALORE_MIN = valoreMin;
        this.tipo="N";
        this.valore = 0;
    }

    /**Permette di modificare estremo superiore del dominio in cui cade l'informazione
     * @param VALORE_MAX nuovo valore massimo possibile per l'informazione
     */
    public void setVALORE_MAX(int VALORE_MAX) {
        this.VALORE_MAX = VALORE_MAX;
    }

    /**Permette di modificare estremo inferiore del dominio in cui cade l'informazione
     * @param VALORE_MIN nuovo valore minimo possibile per l'informazione
     */
    public void setVALORE_MIN(int VALORE_MIN) {
        this.VALORE_MIN = VALORE_MIN;
    }

    /**
     * Genera un nuovo valore/misura assunto dall'informazione,un valore casuale numerico che cade entro il dominio specificato
     */
    public void aggiornaValore() {
        this.valore =  (int) (Math.random() * (this.VALORE_MAX - this.VALORE_MIN) + this.VALORE_MIN);
    }

    /**Fornisce la misura/valore attuale dell'informazione
     * @return valore numerico dell'informazione
     */
    public Object getValore(){
        return this.valore;
    }

    /**Fornisce estremo superiore del dominio in cui cade l'informazione
     * @return valore massimo possibile per l'informazione
     */
    public int getVALORE_MAX() {
        return VALORE_MAX;
    }

    /**Fornisce estremo inferiore del dominio in cui cade l'informazione
     * @return valore minimo possibile per l'informazione
     */
    public int getVALORE_MIN() {
        return VALORE_MIN;
    }

    /**Fornisce nome con cui è specificata dell'informazione
     * @return valore massimo possibile per l'informazione
     */
    public String getNome() {
        return nome;
    }
    /**Permette di modificare il nome con cui è specificata dell'informazione
     * @param nome  nuovo nome da assegnare all'informazione
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**Permette di specificare manualmente il valore assunto dall'informazione
     * @param valore nuovo valore che l'informazione assume
     */
    public void setValore(Object valore) {
        this.valore = valore;
    }

    @Override
    public String toString() {
        return "[" + this.nome + " : " + this.getValore() + "]";
    }

    /**Restituisce il tipo dell'informazione
     * @return il tipo dell'informazione(N = numerica, NN = non numerica)
     */
    public String getTipo() {
        return tipo;
    }

    /**Permette di specificare il tipo dell'informazione
     * @param tipo nuovo tipo dell'informazione
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Informazione clone = (Informazione) super.clone();
        clone.setVALORE_MAX(this.getVALORE_MAX());
        clone.setVALORE_MIN(this.getVALORE_MIN());
        clone.setTipo(this.getTipo());
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Informazione that = (Informazione) o;

        if (getVALORE_MAX() != that.getVALORE_MAX()) return false;
        if (getVALORE_MIN() != that.getVALORE_MIN()) return false;
        if (!getNome().equals(that.getNome())) return false;
        if (!getTipo().equals(that.getTipo())) return false;
        return getValore().equals(that.getValore());
    }

    @Override
    public int hashCode() {
        int result = getNome().hashCode();
        result = 31 * result + getTipo().hashCode();
        result = 31 * result + getValore().hashCode();
        result = 31 * result + getVALORE_MAX();
        result = 31 * result + getVALORE_MIN();
        return result;
    }
}
