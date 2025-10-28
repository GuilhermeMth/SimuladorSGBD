package app.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
// Esta classe é responsável por traduzir comandos SQL em ações do nosso modelo de dados.

public class InterpretadorSQL {

    // Referência única ao nosso banco de dados simulado (Singleton).
    private final BancoSimulado banco;

    public InterpretadorSQL() {
        // Obtém a instância única do banco ao inicializar o interpretador.
        this.banco = BancoSimulado.getInstancia();
    }

    /**
     * Ponto de entrada para execução de comandos SQL.
     * @param sql O comando SQL em formato String.
     * @return O resultado da operação (mensagem ou Tabela de resultado).
     */
    public Object executar(String sql) throws Exception {
        // Pré-processamento: normaliza o SQL para minúsculas e remove espaços extras.
        sql = sql.trim().replaceAll("\\s+", " ").toLowerCase();

        // O interpretador decide qual método de processamento chamar com base no prefixo.
        if (sql.startsWith("create table")) {
            processarCreateTable(sql);
            return "Tabela criada com sucesso!";
        } else if (sql.startsWith("drop table")) {
            processarDropTable(sql);
            return "Tabela removida com sucesso!";
        } else if (sql.startsWith("insert into")) {
            processarInsert(sql);
            return "Linha inserida com sucesso!";
        } else if (sql.startsWith("delete from")) {
            int linhasAfetadas = processarDelete(sql);
            return "Comando DELETE executado com sucesso! Linhas afetadas: " + linhasAfetadas;
        } else if (sql.startsWith("select")) {
            return processarSelect(sql);
        }

        throw new Exception("Comando SQL inválido ou não suportado: '" + sql + "'");
    }

    /**
     * Analisa o comando CREATE TABLE e delega a criação para o BancoSimulado.
     */
    private void processarCreateTable(String sql) throws Exception {
        // Regex para extrair nome da tabela e a string de definição das colunas.
        Pattern p = Pattern.compile("create table ([a-z0-9_]+) \\((.+)\\)");
        Matcher m = p.matcher(sql);

        if (!m.matches()) throw new Exception("Sintaxe de CREATE TABLE inválida.");

        String nomeTabela = m.group(1);
        String defsColunaStr = m.group(2);

        Tabela novaTabela = new Tabela(nomeTabela);

        // Separa as definições de coluna, ignorando vírgulas dentro dos parênteses da FK.
        String[] defs = defsColunaStr.split(",(?![^(]*\\))");

        // Itera sobre cada definição de coluna (e.g., "id INT primary key", "nome STRING")
        for (String def : defs) {
            def = def.trim();
            String[] partes = def.split(" ");

            String nomeColuna = partes[0];
            String tipoDado = partes[1].toUpperCase();

            if (!tipoDado.equals("INT") && !tipoDado.equals("STRING")) {
                throw new Exception("Tipo de dado '" + tipoDado + "' não suportado. Use INT ou STRING.");
            }

            Coluna novaColuna = new Coluna(nomeColuna, tipoDado);

            // Verifica e define as restrições (PK, FK)
            if (def.contains("primary key")) {
                novaColuna.setAsPrimaryKey();
            }
            if (def.contains("references")) {
                // Regex específico para a sintaxe da chave estrangeira.
                Pattern fkPattern = Pattern.compile("references ([a-z0-9_]+)\\s*\\(([a-z0-9_]+)\\)");
                Matcher fkMatcher = fkPattern.matcher(def);
                if (fkMatcher.find()) {
                    // Extrai tabela e coluna referenciadas.
                    novaColuna.setAsForeignKey(fkMatcher.group(1), fkMatcher.group(2));
                } else {
                    throw new Exception("Sintaxe de FOREIGN KEY inválida para a coluna " + nomeColuna);
                }
            }
            novaTabela.adicionarColuna(novaColuna);
        }
        // Delega a persistência da nova tabela para o BancoSimulado.
        banco.criarTabela(novaTabela);
    }

    /**
     * Analisa o comando DROP TABLE e delega a remoção para o BancoSimulado.
     */
    private void processarDropTable(String sql) throws Exception {
        // Regex simples para extrair o nome da tabela.
        Pattern p = Pattern.compile("drop table ([a-z0-9_]+)");
        Matcher m = p.matcher(sql);

        if (!m.matches()) {
            throw new Exception("Sintaxe de DROP TABLE inválida. Use: DROP TABLE nome_tabela");
        }

        String nomeTabela = m.group(1);

        // Delega a remoção e checagem de FKs para o BancoSimulado.
        banco.removerTabela(nomeTabela);
    }

    /**
     * Analisa o comando INSERT INTO, valida os dados e insere a linha na Tabela.
     */
    private void processarInsert(String sql) throws Exception {
        // Regex para extrair nome da tabela, lista de colunas e lista de valores.
        Pattern p = Pattern.compile("insert into ([a-z0-9_]+) \\((.+)\\) values \\((.+)\\)");
        Matcher m = p.matcher(sql);

        if (!m.matches()) throw new Exception("Sintaxe de INSERT INTO inválida.");

        String nomeTabela = m.group(1);
        String[] colunas = m.group(2).split(",");
        String[] valores = m.group(3).split(",");

        if (colunas.length != valores.length) throw new Exception("Número de colunas e valores não coincide.");

        Tabela tabela = banco.getTabela(nomeTabela);
        if (tabela == null) throw new Exception("Tabela '" + nomeTabela + "' não encontrada.");

        // Cria a linha, cujo array interno deve ter o tamanho total das colunas da tabela.
        Linha novaLinha = new Linha(tabela.getNumColunas());

        // Mapeia os valores fornecidos para os índices corretos da tabela.
        for (int i = 0; i < colunas.length; i++) {
            String nomeColuna = colunas[i].trim();
            String valorStr = valores[i].trim();

            int indiceColuna = tabela.getIndiceColunaPeloNome(nomeColuna);
            if (indiceColuna == -1) {
                throw new Exception("Coluna '" + nomeColuna + "' não encontrada na tabela '" + nomeTabela + "'.");
            }

            Coluna coluna = tabela.getColunaPorIndice(indiceColuna);

            // Conversão e validação de tipos.
            Object valor;
            if (coluna.getTipoDado().equals("INT")) {
                try {
                    valor = Integer.parseInt(valorStr);
                } catch (NumberFormatException e) {
                    throw new Exception("Erro de tipo: O valor '" + valorStr + "' não é um INT para a coluna '" + nomeColuna + "'.");
                }
            } else { // STRING
                valor = valorStr.replace("'", ""); // Remove aspas simples da string.
            }

            // Define o dado no índice correto da Linha.
            novaLinha.setDadoPorIndice(indiceColuna, valor);
        }
        // Delega a adição da Linha à Tabela, onde ocorrerão as validações finais (PK, FK).
        tabela.adicionarLinha(novaLinha);
    }

    /**
     * Decide se o comando SELECT é simples ou com JOIN e executa o método apropriado.
     */
    private Tabela processarSelect(String sql) throws Exception {
        // Tenta fazer o match com o padrão de JOIN (mais complexo).
        Pattern joinPattern = Pattern.compile("select (.+) from ([a-z0-9_]+) join ([a-z0-9_]+) on ([a-z0-9_]+\\.[a-z0-9_]+) = ([a-z0-9_]+\\.[a-z0-9_]+)");
        Matcher joinMatcher = joinPattern.matcher(sql);

        // Padrão para SELECT simples.
        Pattern simplePattern = Pattern.compile("select (.+) from ([a-z0-9_]+)");
        Matcher simpleMatcher = simplePattern.matcher(sql);

        if (joinMatcher.matches()) {
            return executarJoin(joinMatcher);
        } else if (simpleMatcher.matches()) {
            String colunasStr = simpleMatcher.group(1).trim();
            String nomeTabela = simpleMatcher.group(2).trim();
            return executarSelectSimples(colunasStr, nomeTabela);
        }

        throw new Exception("Sintaxe de SELECT inválida.");
    }

    /**
     * Analisa o comando DELETE FROM e remove as linhas que atendem à condição WHERE.
     */
    private int processarDelete(String sql) throws Exception {
        // Regex para extrair nome da tabela, coluna e valor da condição WHERE.
        Pattern p = Pattern.compile("delete from ([a-z0-9_]+) where ([a-z0-9_]+) = (.+)");
        Matcher m = p.matcher(sql);

        if (!m.matches()) {
            throw new Exception("Sintaxe de DELETE FROM inválida. Use: DELETE FROM tabela WHERE coluna = valor");
        }

        String nomeTabela = m.group(1);
        String nomeColuna = m.group(2).trim();
        String valorStr = m.group(3).trim();

        Tabela tabela = banco.getTabela(nomeTabela);
        if (tabela == null) throw new Exception("Tabela '" + nomeTabela + "' não encontrada.");

        // Converte o valor de busca para o tipo de dado correto da coluna.
        int indiceColuna = tabela.getIndiceColunaPeloNome(nomeColuna);
        if (indiceColuna == -1) throw new Exception("Coluna '" + nomeColuna + "' não encontrada.");

        Coluna coluna = tabela.getColunaPorIndice(indiceColuna);

        Object valorBusca;
        if (coluna.getTipoDado().equals("INT")) {
            try {
                valorBusca = Integer.parseInt(valorStr);
            } catch (NumberFormatException e) {
                throw new Exception("Erro de tipo: O valor '" + valorStr + "' não é um INT para a coluna '" + nomeColuna + "'.");
            }
        } else { // STRING
            valorBusca = valorStr.replace("'", ""); // Remove aspas
        }

        // Delega a lógica de remoção de linhas para a Tabela, retornando o número de linhas afetadas.
        return tabela.removerLinhas(nomeColuna, valorBusca);
    }

    /**
     * Executa um SELECT sem JOIN. Cria uma Tabela de resultado com as colunas e linhas solicitadas.
     */
    private Tabela executarSelectSimples(String colunasStr, String nomeTabela) throws Exception {
        Tabela tabelaOriginal = banco.getTabela(nomeTabela);
        if (tabelaOriginal == null) throw new Exception("Tabela '" + nomeTabela + "' não encontrada.");

        // Cria a tabela de resultado, que é temporária.
        Tabela resultado = new Tabela("resultado_select");

        if (colunasStr.equals("*")) {
            // Seleção de todas as colunas: Copia todas as colunas e todas as linhas.
            for (int i = 0; i < tabelaOriginal.getNumColunas(); i++) {
                resultado.adicionarColuna(tabelaOriginal.getColunaPorIndice(i));
            }

            // Acesso ao Array de Linhas: Para percorrer os dados da tabela original.
            for (int i = 0; i < tabelaOriginal.getNumLinhas(); i++) {
                Linha linhaOriginal = tabelaOriginal.getLinhasArray()[i];

                // Cria uma nova Linha e copia os dados.
                Linha novaLinha = new Linha(tabelaOriginal.getNumColunas());
                for (int j = 0; j < tabelaOriginal.getNumColunas(); j++) {
                    novaLinha.setDadoPorIndice(j, linhaOriginal.getDadoPorIndice(j));
                }
                resultado.adicionarLinha(novaLinha);
            }
        } else {
            // Seleção de colunas específicas.
            String[] nomesColunasSelecionadas = colunasStr.split(",");

            // 1. Mapeamento de Colunas: Determina quais índices da tabela original serão usados.
            int[] indicesSelecionados = new int[nomesColunasSelecionadas.length];
            for(int i = 0; i < nomesColunasSelecionadas.length; i++) {
                String nome = nomesColunasSelecionadas[i].trim();
                int indice = tabelaOriginal.getIndiceColunaPeloNome(nome);
                if (indice == -1) {
                    throw new Exception("Coluna '" + nome + "' não encontrada.");
                }
                indicesSelecionados[i] = indice;
                // Adiciona a coluna correspondente à Tabela de resultado.
                resultado.adicionarColuna(tabelaOriginal.getColunaPorIndice(indice));
            }

            // 2. Projeção de Linhas: Cria novas Linhas apenas com os dados selecionados.
            for(int i = 0; i < tabelaOriginal.getNumLinhas(); i++) {
                Linha linhaOriginal = tabelaOriginal.getLinhasArray()[i];
                // A nova linha terá apenas o número de colunas selecionadas.
                Linha novaLinha = new Linha(indicesSelecionados.length);

                for(int j = 0; j < indicesSelecionados.length; j++) {
                    int indiceOriginal = indicesSelecionados[j];
                    // Transfere o dado usando o índice mapeado.
                    novaLinha.setDadoPorIndice(j, linhaOriginal.getDadoPorIndice(indiceOriginal));
                }
                resultado.adicionarLinha(novaLinha);
            }
        }
        return resultado;
    }

    /**
     * Executa um JOIN entre duas tabelas (INNER JOIN).
     */
    private Tabela executarJoin(Matcher matcher) throws Exception {
        String colunasStr = matcher.group(1).trim();
        String nomeTabela1 = matcher.group(2);
        String nomeTabela2 = matcher.group(3);
        String condicao1 = matcher.group(4); // e.g., 't1.id'
        String condicao2 = matcher.group(5); // e.g., 't2.ref_id'

        Tabela t1 = banco.getTabela(nomeTabela1);
        if (t1 == null) throw new Exception("Tabela '" + nomeTabela1 + "' não encontrada.");
        Tabela t2 = banco.getTabela(nomeTabela2);
        if (t2 == null) throw new Exception("Tabela '" + nomeTabela2 + "' não encontrada.");

        String[] c1_parts = condicao1.split("\\.");
        String[] c2_parts = condicao2.split("\\.");

        // Simplesmente extraímos o nome da coluna ignorando o prefixo da tabela (que é o que a regex extraiu)
        String colNameT1 = c1_parts[1];
        String colNameT2 = c2_parts[1];

        // Determina os índices de busca nas tabelas originais.
        int indiceJoinT1 = t1.getIndiceColunaPeloNome(colNameT1);
        int indiceJoinT2 = t2.getIndiceColunaPeloNome(colNameT2);

        if (indiceJoinT1 == -1) throw new Exception("Coluna '" + colNameT1 + "' não encontrada na tabela '" + t1.getNome() + "'.");
        if (indiceJoinT2 == -1) throw new Exception("Coluna '" + colNameT2 + "' não encontrada na tabela '" + t2.getNome() + "'.");

        Tabela resultado = new Tabela("resultado_join");

        // 1. Definição do Esquema de Resultado (Colunas de T1 + Colunas de T2)
        int numColunasT1 = t1.getNumColunas();
        for (int i = 0; i < numColunasT1; i++) {
            Coluna c = t1.getColunaPorIndice(i);
            // Renomeia colunas para evitar conflitos (ex: t1.id, t2.id)
            resultado.adicionarColuna(new Coluna(t1.getNome() + "." + c.getNome(), c.getTipoDado()));
        }
        int numColunasT2 = t2.getNumColunas();
        for (int i = 0; i < numColunasT2; i++) {
            Coluna c = t2.getColunaPorIndice(i);
            resultado.adicionarColuna(new Coluna(t2.getNome() + "." + c.getNome(), c.getTipoDado()));
        }
        int totalColunas = resultado.getNumColunas();

        // 2. Execução do Loop de JOIN (Nested Loop Join)
        Linha[] linhasT1 = t1.getLinhasArray();
        Linha[] linhasT2 = t2.getLinhasArray();

        // Itera sobre as linhas da Tabela 1 (Loop Externo)
        for (int i = 0; i < t1.getNumLinhas(); i++) {
            Linha l1 = linhasT1[i];

            // Itera sobre as linhas da Tabela 2 (Loop Interno)
            for (int j = 0; j < t2.getNumLinhas(); j++) {
                Linha l2 = linhasT2[j];

                // Compara os valores nas colunas de JOIN (condição ON)
                Object valor1 = l1.getDadoPorIndice(indiceJoinT1);
                Object valor2 = l2.getDadoPorIndice(indiceJoinT2);

                if (valor1 != null && valor1.equals(valor2)) {
                    // Match encontrado: Cria a linha de resultado combinada.
                    Linha novaLinha = new Linha(totalColunas);
                    int indiceResultado = 0;

                    // Copia todos os dados da Tabela 1
                    for (int k = 0; k < numColunasT1; k++) {
                        novaLinha.setDadoPorIndice(indiceResultado++, l1.getDadoPorIndice(k));
                    }

                    // Copia todos os dados da Tabela 2
                    for (int k = 0; k < numColunasT2; k++) {
                        novaLinha.setDadoPorIndice(indiceResultado++, l2.getDadoPorIndice(k));
                    }

                    resultado.adicionarLinha(novaLinha);
                }
            }
        }

        // A implementação não filtra as colunas selecionadas em 'colunasStr', apenas retorna o JOIN completo.

        return resultado;
    }
}