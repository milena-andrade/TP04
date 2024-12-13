import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class GerenciadorFuncionarios extends JFrame {
    private JTextField campoBusca;
    private JLabel labelNome, labelSalario, labelCargo;
    private JTextField campoNome, campoSalario, campoCargo;
    private JButton botaoBuscar, botaoAnterior, botaoProximo;
    private Connection conexao;
    private PreparedStatement comando;
    private ResultSet conjuntoResultados;
    private ArrayList<String> historicoBuscas = new ArrayList<>();
    private int indiceHistorico = -1;

    public GerenciadorFuncionarios() {
        setTitle("Gerenciador de Funcionários");
        setLayout(null);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel rotuloBusca = new JLabel("Nome:");
        rotuloBusca.setBounds(20, 20, 50, 20);
        add(rotuloBusca);

        campoBusca = new JTextField();
        campoBusca.setBounds(80, 20, 200, 20);
        add(campoBusca);

        botaoBuscar = new JButton("Buscar");
        botaoBuscar.setBounds(290, 20, 100, 20);
        add(botaoBuscar);

        labelNome = new JLabel("Nome:");
        labelNome.setBounds(20, 60, 50, 20);
        add(labelNome);

        campoNome = new JTextField();
        campoNome.setBounds(80, 60, 200, 20);
        campoNome.setEditable(false);
        add(campoNome);

        labelSalario = new JLabel("Salário:");
        labelSalario.setBounds(20, 100, 50, 20);
        add(labelSalario);

        campoSalario = new JTextField();
        campoSalario.setBounds(80, 100, 200, 20);
        campoSalario.setEditable(false);
        add(campoSalario);

        labelCargo = new JLabel("Cargo:");
        labelCargo.setBounds(20, 140, 50, 20);
        add(labelCargo);

        campoCargo = new JTextField();
        campoCargo.setBounds(80, 140, 200, 20);
        campoCargo.setEditable(false);
        add(campoCargo);

        botaoAnterior = new JButton("Anterior");
        botaoAnterior.setBounds(50, 200, 100, 30);
        add(botaoAnterior);

        botaoProximo = new JButton("Próximo");
        botaoProximo.setBounds(200, 200, 100, 30);
        add(botaoProximo);

        botaoBuscar.addActionListener(e -> buscarFuncionario());
        botaoAnterior.addActionListener(e -> navegarHistorico(false));
        botaoProximo.addActionListener(e -> navegarHistorico(true));

        setVisible(true);
    }

    private void buscarFuncionario() {
        String nome = campoBusca.getText();

        try {
            if (conjuntoResultados != null) conjuntoResultados.close();
            if (comando != null) comando.close();
            if (conexao != null) conexao.close();

            conexao = DriverManager.getConnection("jdbc:mysql://localhost:3306/tp04", "root", "");
            comando = conexao.prepareStatement(
                "SELECT f.nome_func, f.sal_func, c.ds_cargo " +
                "FROM tbfuncs f " +
                "JOIN tbcargos c ON f.cod_cargo = c.cd_cargo " +
                "WHERE f.nome_func LIKE ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            comando.setString(1, "%" + nome + "%");
            conjuntoResultados = comando.executeQuery();

            if (conjuntoResultados.next()) {
                if (historicoBuscas.isEmpty() || !historicoBuscas.get(historicoBuscas.size() - 1).equals(nome)) {
                    historicoBuscas.add(nome);
                    indiceHistorico = historicoBuscas.size() - 1;
                }
                exibirInformacoes();
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum registro encontrado.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao buscar: " + ex.getMessage());
        }
    }

    private void navegarHistorico(boolean avancar) {
        if (historicoBuscas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma busca realizada.");
            return;
        }

        if (avancar) {
            if (indiceHistorico < historicoBuscas.size() - 1) {
                indiceHistorico++;
                executarBuscaHistorico();
            } else {
                JOptionPane.showMessageDialog(this, "Não há mais registros nessa direção.");
            }
        } else {
            if (indiceHistorico > 0) {
                indiceHistorico--;
                executarBuscaHistorico();
            } else {
                JOptionPane.showMessageDialog(this, "Não há mais registros nessa direção.");
            }
        }
    }

    private void executarBuscaHistorico() {
        String nome = historicoBuscas.get(indiceHistorico);

        try {
            if (conjuntoResultados != null) conjuntoResultados.close();
            if (comando != null) comando.close();

            comando = conexao.prepareStatement(
                "SELECT f.nome_func, f.sal_func, c.ds_cargo " +
                "FROM tbfuncs f " +
                "JOIN tbcargos c ON f.cod_cargo = c.cd_cargo " +
                "WHERE f.nome_func LIKE ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            comando.setString(1, "%" + nome + "%");
            conjuntoResultados = comando.executeQuery();

            if (conjuntoResultados.next()) {
                exibirInformacoes();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao executar busca no histórico: " + ex.getMessage());
        }
    }

    private void exibirInformacoes() throws SQLException {
        campoNome.setText(conjuntoResultados.getString("nome_func"));
        campoSalario.setText(String.valueOf(conjuntoResultados.getBigDecimal("sal_func")));
        campoCargo.setText(conjuntoResultados.getString("ds_cargo"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GerenciadorFuncionarios::new);
    }
}
