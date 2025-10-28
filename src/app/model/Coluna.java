package app.model;

public class Coluna {
    private String nome;
    private String tipoDado; // "INT" ou "STRING"
    private boolean isPrimaryKey = false;
    private ForeignKeyConstraint fkConstraint = null;

    public Coluna(String nome, String tipoDado) {
        this.nome = nome.toLowerCase();
        this.tipoDado = tipoDado.toUpperCase();
    }

    // Getters
    public String getNome() { return nome; }
    public String getTipoDado() { return tipoDado; }
    public boolean isPrimaryKey() { return isPrimaryKey; }
    public ForeignKeyConstraint getFkConstraint() { return fkConstraint; }

    // Setters
    public void setAsPrimaryKey() { this.isPrimaryKey = true; }
    public void setAsForeignKey(String refTable, String refColumn) {
        this.fkConstraint = new ForeignKeyConstraint(refTable, refColumn);
    }

    @Override
    public String toString() {
        String info = nome + " (" + tipoDado + ")";
        if (isPrimaryKey) info += " [PK]";
        if (fkConstraint != null) info += " [FK]";
        return info;
    }
}