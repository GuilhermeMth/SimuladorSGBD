package app.model;

public class BancoSimulado {

    // Define a capacidade inicial do nosso array.
    // Arrays clássicos em Java têm tamanho fixo.
    private static final int CAPACIDADE_INICIAL = 5;

    private static BancoSimulado instancia;

    // ----- Gerenciamento com Array Clássico -----

    // 1. O Array de Tabelas:
    // Este é um array clássico (estático) para armazenar as tabelas.
    // Sua capacidade (tabelas.length) é fixa após a inicialização.
    private Tabela[] tabelas;

    // 2. O Contador de Elementos:
    // Como 'tabelas.length' é apenas a *capacidade total* (ex: 5, 10, 20...),
    // precisamos de uma variável ('numTabelas') para rastrear quantos slots
    // estão *realmente ocupados* (ex: 0, 1, 2...).
    private int numTabelas;
    // ---------------------------------------------

    private BancoSimulado() {
        // Inicializa o array clássico com a capacidade fixa definida.
        this.tabelas = new Tabela[CAPACIDADE_INICIAL];

        // No início, o array está vazio (0 elementos ocupados).
        this.numTabelas = 0;
    }

    public static synchronized BancoSimulado getInstancia() {
        if (instancia == null) {
            instancia = new BancoSimulado();
        }
        return instancia;
    }

    // --- Lógica de Redimensionamento (Realocação) ---

    /**
     * Demonstra a principal característica de usar arrays clássicos:
     * Quando o array fica cheio, precisamos criar um NOVO array, maior,
     * e copiar manualmente todos os elementos do antigo para o novo.
     */
    private void redimensionarTabelas() {
        // 1. Define uma nova capacidade (neste caso, o dobro).
        int novaCapacidade = tabelas.length * 2;

        // 2. Aloca um *novo* array na memória com a nova capacidade.
        Tabela[] novoArrayTabelas = new Tabela[novaCapacidade];

        // 3. Copia os elementos, um por um, do array antigo para o novo.
        // Usamos 'numTabelas' para saber quantos elementos precisam ser copiados.
        for (int i = 0; i < numTabelas; i++) {
            novoArrayTabelas[i] = this.tabelas[i];
        }

        // 4. Substitui a referência do array antigo pelo novo array.
        this.tabelas = novoArrayTabelas;
    }

    // --- Métodos de Manipulação (CREATE TABLE) ---

    public void criarTabela(Tabela tabela) throws Exception {
        String nomeTabela = tabela.getNome().toLowerCase();

        // Busca em Array: Iteramos manualmente (loop for) sobre os elementos
        // ocupados (de 0 até 'numTabelas') para verificar se a tabela já existe.
        for (int i = 0; i < numTabelas; i++) {
            if (this.tabelas[i].getNome().equals(nomeTabela)) {
                throw new Exception("Tabela '" + nomeTabela + "' já existe.");
            }
        }

        // Verificação de Capacidade: Checamos se o número de elementos ocupados
        // ('numTabelas') atingiu a capacidade total do array ('tabelas.length').
        if (numTabelas == tabelas.length) {
            // Se sim, precisamos "aumentar" o array antes de inserir.
            redimensionarTabelas();
        }

        // Inserção em Array: A nova tabela é adicionada no primeiro índice
        // livre disponível, que é sempre o índice 'numTabelas'.
        this.tabelas[numTabelas] = tabela;

        // Incrementamos o contador de elementos ocupados.
        this.numTabelas++;
    }

    // --- Métodos de Manipulação (DROP TABLE) ---

    public void removerTabela(String nome) throws Exception {
        String nomeTabela = nome.toLowerCase();

        // 1. Verificação de Integridade (Busca em Array)
        // Itera sobre todas as tabelas (de 0 a 'numTabelas').
        for (int i = 0; i < numTabelas; i++) {
            Tabela tabelaExistente = this.tabelas[i];

            // Itera sobre as colunas da tabela atual.
            for (int j = 0; j < tabelaExistente.getNumColunas(); j++) {
                Coluna coluna = tabelaExistente.getColunaPorIndice(j);

                if (coluna != null && coluna.getFkConstraint() != null) {
                    if (coluna.getFkConstraint().getTabelaReferenciada().equalsIgnoreCase(nomeTabela)) {
                        throw new Exception("Não é possível remover a tabela '" + nomeTabela + "'. Ela está sendo referenciada pela chave estrangeira na tabela '" + tabelaExistente.getNome() + "'.");
                    }
                }
            }
        }

        // 2. Busca e Remoção (Manipulação de Array)

        // Primeiro, encontramos o índice do elemento a ser removido.
        // Novamente, usamos uma busca linear de 0 até 'numTabelas'.
        int indiceRemover = -1;
        for (int i = 0; i < numTabelas; i++) {
            if (this.tabelas[i].getNome().equals(nomeTabela)) {
                indiceRemover = i;
                break; // Encontramos, podemos parar o loop.
            }
        }

        if (indiceRemover == -1) {
            throw new Exception("Tabela '" + nomeTabela + "' não encontrada.");
        }

        // Estratégia de Remoção em Array (Sem "buracos"):
        // Para evitar criar um "buraco" (null) no meio do array, o que
        // quebraria nossos loops (que vão de 0 a 'numTabelas'), nós
        // pegamos o *último* elemento ocupado (índice 'numTabelas - 1')
        // e o movemos para a posição do elemento que queremos remover.

        int indiceUltimoElemento = numTabelas - 1;

        if (indiceRemover < indiceUltimoElemento) {
            // Move o último elemento para a posição do item removido.
            this.tabelas[indiceRemover] = this.tabelas[indiceUltimoElemento];
        }

        // Limpa a referência do último elemento (agora duplicada ou já removida).
        // Isso ajuda o Coletor de Lixo (Garbage Collector).
        this.tabelas[indiceUltimoElemento] = null;

        // Finalmente, decrementamos o contador de elementos ocupados.
        this.numTabelas--;
    }

    // --- Getter ---

    /**
     * Retorna a tabela pelo nome ou null se não for encontrada.
     * @param nome O nome da tabela.
     * @return O objeto Tabela ou null.
     */
    public Tabela getTabela(String nome) {
        String nomeBusca = nome.toLowerCase();

        // Busca em Array: Mais uma vez, uma busca linear
        // iterando de 0 até o número de elementos ocupados ('numTabelas').
        for (int i = 0; i < numTabelas; i++) {
            if (this.tabelas[i].getNome().equals(nomeBusca)) {
                return this.tabelas[i]; // Retorna o elemento encontrado.
            }
        }

        // Se o loop terminar sem encontrar, significa que o elemento não existe.
        return null;
    }
}