package app.model;

// Removendo imports de Collections e Stream API

/**
 * Representa uma tabela no banco de dados, contendo a definição das colunas e os dados (linhas).
 * Gerencia o armazenamento de dados usando arrays clássicos (Arrays de Java).
 */
public class Tabela {
    private static final int CAPACIDADE_INICIAL = 10;

    private String nome;

    // --- Armazenamento de Estrutura (Colunas) ---
    // Array Clássico: Armazena os metadados de cada coluna da tabela (definição).
    private Coluna[] colunas;
    // Contador: Rastreia o número real de colunas utilizadas no array 'colunas'.
    private int numColunas;

    // Array Clássico: Mapeamento Nome da Coluna -> Índice.
    // Usado para buscas rápidas (ex: getIndiceColunaPeloNome).
    private String[] nomesColunas;

    // --- Armazenamento de Dados (Linhas) ---
    // Array Clássico: Armazena os registros de dados (objetos Linha).
    private Linha[] linhas;
    // Contador: Rastreia o número real de linhas utilizadas no array 'linhas'.
    private int numLinhas;

    public Tabela(String nome) {
        this.nome = nome.toLowerCase();

        // Inicializando todos os arrays clássicos com capacidade fixa inicial
        this.colunas = new Coluna[CAPACIDADE_INICIAL];
        this.linhas = new Linha[CAPACIDADE_INICIAL];
        this.nomesColunas = new String[CAPACIDADE_INICIAL];

        this.numColunas = 0;
        this.numLinhas = 0;
    }

    // --- Lógica de Redimensionamento (Colunas) ---

    /**
     * Redimensiona os arrays 'colunas' e 'nomesColunas' quando a capacidade é excedida.
     * Envolve a criação de um novo array de tamanho maior e a cópia manual de elementos.
     */
    private void redimensionarColunas() {
        int novaCapacidade = colunas.length * 2;

        // 1. Redimensiona o array de Colunas (Estrutura)
        Coluna[] novoColunas = new Coluna[novaCapacidade];
        // Cópia de elementos usando loop for clássico
        for (int i = 0; i < numColunas; i++) {
            novoColunas[i] = this.colunas[i];
        }
        this.colunas = novoColunas; // Substitui a referência para o novo array

        // 2. Redimensiona o array de Mapeamento de Nomes (Busca)
        String[] novoNomesColunas = new String[novaCapacidade];
        // Cópia de elementos usando loop for clássico
        for (int i = 0; i < numColunas; i++) {
            novoNomesColunas[i] = this.nomesColunas[i];
        }
        this.nomesColunas = novoNomesColunas; // Substitui a referência para o novo array
    }

    /**
     * Adiciona uma nova coluna à estrutura da tabela.
     */
    public void adicionarColuna(Coluna coluna) {
        if (numColunas == colunas.length) {
            // Verifica se o array clássico atingiu o limite e redimensiona.
            redimensionarColunas();
        }

        // Adiciona o elemento ao final dos arrays e incrementa o contador.
        this.colunas[numColunas] = coluna;
        this.nomesColunas[numColunas] = coluna.getNome();

        this.numColunas++;
    }

    // --- Lógica de Redimensionamento (Linhas) ---

    /**
     * Redimensiona o array 'linhas' quando a capacidade é excedida.
     * Envolve a criação de um novo array de tamanho maior e a cópia manual de elementos.
     */
    private void redimensionarLinhas() {
        int novaCapacidade = linhas.length * 2;
        Linha[] novoLinhas = new Linha[novaCapacidade];

        // Copia os elementos usando loop for clássico
        for (int i = 0; i < numLinhas; i++) {
            novoLinhas[i] = this.linhas[i];
        }
        this.linhas = novoLinhas; // Substitui a referência para o novo array
    }

    /**
     * Adiciona uma nova linha (registro) à tabela, após validações de restrições.
     */
    public void adicionarLinha(Linha linha) throws Exception {
        // O método no InterpretadorSQL deve garantir que a Linha refatorada
        // já tenha seus dados preenchidos nos índices corretos.

        // 1. Validar Chave Primária (Unicidade)
        for (int i = 0; i < numColunas; i++) {
            Coluna pkColuna = colunas[i];
            if (pkColuna.isPrimaryKey()) {
                int pkIndice = i;
                Object novoValor = linha.getDadoPorIndice(pkIndice);

                if (novoValor == null) {
                    throw new Exception("Erro de violação de chave primária: valor não pode ser nulo para a coluna '" + pkColuna.getNome() + "'.");
                }

                // Busca manual (loop for) em todo o array de linhas para checar duplicatas de PK.
                for (int j = 0; j < numLinhas; j++) {
                    Linha linhaExistente = linhas[j];
                    if (linhaExistente.getDadoPorIndice(pkIndice).equals(novoValor)) {
                        throw new Exception("Erro de violação de chave primária: valor '" + novoValor + "' já existe para a coluna '" + pkColuna.getNome() + "'.");
                    }
                }
            }
        }

        // 2. Validar Chave Estrangeira (Existência)
        for (int i = 0; i < numColunas; i++) {
            Coluna fkColuna = colunas[i];
            if (fkColuna.getFkConstraint() != null) {
                int fkIndice = i;
                Object valorFk = linha.getDadoPorIndice(fkIndice);

                // Permite FKs nulas
                if (valorFk == null) continue;

                ForeignKeyConstraint constraint = fkColuna.getFkConstraint();

                Tabela tabelaReferenciada = BancoSimulado.getInstancia()
                        .getTabela(constraint.getTabelaReferenciada()); // Chamará o novo getTabela refatorado

                if (tabelaReferenciada == null) {
                    throw new Exception("Tabela referenciada '" + constraint.getTabelaReferenciada() + "' não existe.");
                }

                // Busca o índice na tabela referenciada
                int indiceColunaReferenciada = tabelaReferenciada.getIndiceColunaPeloNome(constraint.getColunaReferenciada());

                // Busca manual (loop for) em todo o array de linhas da tabela referenciada.
                boolean referenciaEncontrada = false;
                for (int j = 0; j < tabelaReferenciada.numLinhas; j++) {
                    Linha linhaReferenciada = tabelaReferenciada.linhas[j];
                    Object valorReferenciado = linhaReferenciada.getDadoPorIndice(indiceColunaReferenciada);

                    if (valorReferenciado != null && valorReferenciado.equals(valorFk)) {
                        referenciaEncontrada = true;
                        break;
                    }
                }

                if (!referenciaEncontrada) {
                    throw new Exception("Erro de violação de chave estrangeira: o valor '" + valorFk + "' não existe na tabela '" + constraint.getTabelaReferenciada() + "'.");
                }
            }
        }

        // 3. Insere a linha no array clássico, redimensionando se necessário
        if (numLinhas == linhas.length) {
            redimensionarLinhas();
        }
        this.linhas[numLinhas] = linha;
        this.numLinhas++;
    }

    // --- Métodos de Manipulação (DELETE) ---

    /**
     * Remove linhas da tabela que satisfazem a condição (coluna = valor).
     * @return O número de linhas removidas.
     */
    public int removerLinhas(String nomeColuna, Object valor) throws Exception {
        int indiceBusca = getIndiceColunaPeloNome(nomeColuna);
        if (indiceBusca == -1) {
            throw new Exception("Coluna de busca '" + nomeColuna + "' não encontrada.");
        }

        // A remoção eficiente em array é feita copiando apenas os elementos desejados
        // para um novo array (ou movendo-os internamente). Aqui, criamos um novo array.
        Linha[] novoArrayLinhas = new Linha[linhas.length];
        int novoNumLinhas = 0;
        int linhasRemovidas = 0;

        // Loop for: Itera sobre o array de linhas original
        for (int i = 0; i < numLinhas; i++) {
            Linha linhaAtual = linhas[i];
            Object valorNaLinha = linhaAtual.getDadoPorIndice(indiceBusca);

            // Verifica a condição: Manter se o valor for diferente do valor de busca
            if (valorNaLinha == null || !valorNaLinha.equals(valor)) {
                // Copia a linha que DEVE ser mantida para o novo array
                novoArrayLinhas[novoNumLinhas] = linhaAtual;
                novoNumLinhas++;
            } else {
                // Linha será descartada
                linhasRemovidas++;
            }
        }

        this.linhas = novoArrayLinhas; // O array clássico é substituído
        this.numLinhas = novoNumLinhas; // O contador reflete o novo número de linhas

        // Note: O array 'linhas' antigo será elegível para coleta de lixo.

        return linhasRemovidas;
    }

    // --- Getters e Métodos Auxiliares ---

    public String getNome() { return nome; }
    public int getNumColunas() { return numColunas; }
    public int getNumLinhas() { return numLinhas; }

    /** Retorna o array de Colunas (Estrutura da Tabela) */
    public Coluna[] getColunasArray() { return colunas; }

    /** Retorna o array de Linhas (Dados da Tabela) */
    public Linha[] getLinhasArray() {
        // Retorna o array clássico. Note que este array pode conter slots 'null' no final,
        // mas o numLinhas informa quantos elementos válidos existem.
        return linhas;
    }

    /** * Implementação manual de busca: Procura o índice de uma coluna pelo nome,
     * usando o array de mapeamento.
     * @return O índice (0 a numColunas-1) ou -1 se não encontrar.
     */
    public int getIndiceColunaPeloNome(String nomeColuna) {
        String nomeBusca = nomeColuna.toLowerCase().trim();
        // Loop for: Itera sobre o array de nomes de coluna até o número de colunas existentes.
        for (int i = 0; i < numColunas; i++) {
            if (this.nomesColunas[i].equals(nomeBusca)) {
                return i; // Retorna o índice encontrado
            }
        }
        return -1;
    }

    /** Retorna a coluna pelo índice, acessando o array 'colunas' diretamente. */
    public Coluna getColunaPorIndice(int indice) {
        if (indice >= 0 && indice < numColunas) {
            return colunas[indice];
        }
        return null;
    }
}