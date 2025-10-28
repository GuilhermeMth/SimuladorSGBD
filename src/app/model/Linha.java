package app.model;

/**
 * Representa um registro (linha) numa tabela.
 * Armazena os valores dos campos usando um array clássico de Object.
 */
public class Linha {
    // Array clássico de Object para armazenar os valores (INT ou STRING).
    // O acesso deve ser feito APENAS por índice (posição da coluna).
    private Object[] dadosArray;

    /**
     * Construtor que inicializa a linha com um array de dados.
     * @param tamanho O número de colunas que esta linha deve armazenar.
     */
    public Linha(int tamanho) {
        if (tamanho <= 0) {
            // Em SGBDs, uma linha deve ter pelo menos 1 coluna
            this.dadosArray = new Object[1];
        } else {
            this.dadosArray = new Object[tamanho];
        }
    }

    /**
     * Define um valor no array na posição (índice) especificada.
     * Esta função é chamada pela Tabela/Interpretador para preencher o registro.
     * @param indice O índice (posição) da coluna.
     * @param valor O valor a ser armazenado.
     */
    public void setDadoPorIndice(int indice, Object valor) {
        if (indice < 0 || indice >= dadosArray.length) {
            // Se o índice for inválido, lança uma exceção (runtime, pois não deveria ocorrer)
            throw new ArrayIndexOutOfBoundsException("Índice de coluna fora do limite da linha.");
        }
        this.dadosArray[indice] = valor;
    }

    /**
     * Retorna o valor armazenado no índice (coluna) especificado.
     * Esta função é chamada pela Tabela/Interpretador para buscar o valor.
     * @param indice O índice (posição) da coluna.
     * @return O valor.
     */
    public Object getDadoPorIndice(int indice) {
        if (indice < 0 || indice >= dadosArray.length) {
            throw new ArrayIndexOutOfBoundsException("Índice de coluna fora do limite da linha.");
        }
        return this.dadosArray[indice];
    }

    /**
     * Retorna o array clássico de dados (usado para compor o resultado de SELECT).
     */
    public Object[] getDadosArray() {
        return dadosArray;
    }

}