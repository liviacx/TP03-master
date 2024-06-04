import java.util.Scanner;
import java.io.File;

public class Main {

    // Método principal
    public static void main(String[] args) {

        ListaInvertida listaNome = null;
        ListaInvertida listaDia = null;
        ListaInvertida listaDiretor = null;
        Scanner console = new Scanner(System.in);

        try {
            criarArquivo();

            listaNome = new ListaInvertida(4, "dados/dicionario.listainv.db", "dados/blocos.listainv.db");
            listaDia = new ListaInvertida(4, "dados/dicionario2.listainv.db", "dados/blocos2.listainv.db");
            listaDiretor = new ListaInvertida(4, "dados/dicionario3.listainv.db", "dados/blocos3.listainv.db");

            int opcao;
            do {
                opcao = exibirMenu(console);

                Arquivo<Filme> arquivoFilmes = new Arquivo<>("dados/filmes.db", Filme.class.getConstructor());

                switch (opcao) {
                    case 1:
                        inserirFilme(console, listaNome, listaDia, listaDiretor, arquivoFilmes);
                        break;
                    case 2:
                        buscarFilme(console, listaNome, listaDia, listaDiretor);
                        break;
                    case 3:
                        excluirFilme(console, listaNome, listaDia, listaDiretor, arquivoFilmes);
                        break;
                    case 4:
                        imprimirListas(listaNome, listaDia, listaDiretor);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opção inválida");
                }
            } while (opcao != 0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            console.close();
        }
    }

    private static void criarArquivo() {
        File dataDir = new File("dados");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
    }

    private static int exibirMenu(Scanner console) {
        System.out.println("\n\n-------------------------------");
        System.out.println("              MENU");
        System.out.println("-------------------------------");
        System.out.println("1 - Inserir");
        System.out.println("2 - Buscar");
        System.out.println("3 - Excluir");
        System.out.println("4 - Imprimir");
        System.out.println("0 - Sair");

        try {
            return Integer.parseInt(console.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void inserirFilme(Scanner console, ListaInvertida listaNome, ListaInvertida listaDia, ListaInvertida listaDiretor, Arquivo<Filme> arquivoFilmes) throws Exception {
        System.out.println("Digite: nome do filme, dia de lançamento e diretor(a):");
        String nome = console.nextLine();
        int dia = Integer.valueOf(console.nextLine());
        String diretor = console.nextLine();
        Filme filme = new Filme(-1, nome, dia, diretor);
        int dado = arquivoFilmes.create(filme);

        listaNome.create(nome, dado);
        listaDia.create(String.valueOf(dia), dado);
        listaDiretor.create(diretor, dado);

        System.out.println("Filme inserido com sucesso! Nome: " + nome + " Dia: " + dia + " Diretor: " + diretor);
    }


    private static void buscarFilme(Scanner console, ListaInvertida listaNome, ListaInvertida listaDia, ListaInvertida listaDiretor) throws Exception {
        System.out.println("Digite qual tipo de chave quer pesquisar (nome, dia ou diretor):");
        String tipo = console.nextLine();
        int[] dados;

        switch (tipo.toLowerCase()) {
            case "nome":
                System.out.println("Digite o nome do filme:");
                String nome = console.nextLine();
                dados = listaNome.read(nome);
                break;
            case "dia":
                System.out.println("Digite o dia de lançamento do filme:");
                int dia = Integer.parseInt(console.nextLine());
                dados = listaDia.read(String.valueOf(dia));
                break;
            case "diretor":
                System.out.println("Digite o diretor do filme:");
                String diretor = console.nextLine();
                dados = listaDiretor.read(diretor);
                break;
            default:
                System.out.println("Tipo inválido");
                return;
        }

        System.out.print("Dado: ");
        for (int dado : dados) {
            System.out.print(dados + " ");
        }
        System.out.println();
    }

    private static void excluirFilme(Scanner console, ListaInvertida listaNome, ListaInvertida listaDia, ListaInvertida listaDiretor, Arquivo<Filme> arquivoFilmes) throws Exception {
        System.out.println("Digite: nome do filme, dia de lançamento e diretor(a):");
        String nome = console.nextLine();
        int dia = Integer.parseInt(console.nextLine());
        String diretor = console.nextLine();

        int[] idsNome = listaNome.read(nome);
        int[] idsDia = listaDia.read(String.valueOf(dia));
        int[] idsDiretor = listaDiretor.read(diretor);

        int dado  = intersecao(idsNome, idsDia, idsDiretor);
        if (dado  == -1) {
            System.out.println("Filme não encontrado");
            return;
        }

        listaNome.delete(nome, dado);
        listaDia.delete(String.valueOf(dia), dado);
        listaDiretor.delete(diretor, dado);
        arquivoFilmes.delete(dado);
    }

    private static void imprimirListas(ListaInvertida listaNome, ListaInvertida listaDia, ListaInvertida listaDiretor) throws Exception {
        listaNome.print("NOME");
        listaDia.print("DIA");
        listaDiretor.print("DIRETOR");
    }

    public static int intersecao(int[] a, int[] b, int[] c) {
        for (int i : a) {
            for (int j : b) {
                for (int k : c) {
                    if (i == j && j == k) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
