package app.model;

public class ForeignKeyConstraint {
    private String tabelaReferenciada;
    private String colunaReferenciada;

    public ForeignKeyConstraint(String tabelaReferenciada, String colunaReferenciada) {
        this.tabelaReferenciada = tabelaReferenciada;
        this.colunaReferenciada = colunaReferenciada;
    }

    public String getTabelaReferenciada() {
        return tabelaReferenciada;
    }

    public String getColunaReferenciada() {
        return colunaReferenciada;
    }
}