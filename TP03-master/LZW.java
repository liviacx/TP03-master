import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class LZW {

    private static final int TAMANHO_INICIAL_DICIONARIO = 256;

    public void comprimirDiretorio(String dirEntrada, String dirSaida) {
        File diretorio = new File(dirEntrada);
        File[] arquivos = diretorio.listFiles();

        for (File arquivo : arquivos) {
            if (arquivo.isDirectory()) {
                comprimirDiretorio(arquivo.getAbsolutePath(), dirSaida);
            } else {
                comprimirArquivo(arquivo.getAbsolutePath(), dirSaida + "/" + arquivo.getName() + ".lzw");
            }
        }
    }

    public void descomprimirDiretorio(String dirComprimido, String dirSaida) {
        File diretorio = new File(dirComprimido);
        File[] arquivos = diretorio.listFiles();

        for (File arquivo : arquivos) {
            if (arquivo.isDirectory()) {
                descomprimirDiretorio(arquivo.getAbsolutePath(), dirSaida);
            } else {
                descomprimirArquivo(arquivo.getAbsolutePath(), dirSaida + "/" + arquivo.getName().replace(".lzw", ""));
            }
        }
    }

    public void comprimirArquivo(String arquivoEntrada, String arquivoSaida) {
        try (FileInputStream fis = new FileInputStream(arquivoEntrada);
             DataOutputStream dos = new DataOutputStream(new FileOutputStream(arquivoSaida))) {

            Map<String, Integer> dicionario = inicializarDicionario();
            int proximoCodigo = TAMANHO_INICIAL_DICIONARIO;
            StringBuilder palavra = new StringBuilder();
            List<Integer> codigosSaida = new ArrayList<>();

            int caractere;
            while ((caractere = fis.read()) != -1) {
                palavra.append((char) caractere);
                if (!dicionario.containsKey(palavra.toString())) {
                    dicionario.put(palavra.toString(), proximoCodigo++);
                    codigosSaida.add(dicionario.get(palavra.substring(0, palavra.length() - 1)));
                    palavra = new StringBuilder(palavra.substring(palavra.length() - 1));
                }
            }

            if (!palavra.toString().equals("")) {
                codigosSaida.add(dicionario.get(palavra.toString()));
            }

            for (Integer codigo : codigosSaida) {
                dos.writeShort(codigo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void descomprimirArquivo(String arquivoComprimido, String dirSaida) {
        File arquivoDirSaida = new File(dirSaida);
        if (!arquivoDirSaida.exists() || !arquivoDirSaida.isDirectory()) {
            arquivoDirSaida.mkdirs();
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(arquivoComprimido));
             FileOutputStream fos = new FileOutputStream(dirSaida + "/" + new File(arquivoComprimido).getName().replace(".lzw", ""))) {

            Map<Integer, String> dicionario = inicializarDicionarioReverso();
            int proximoCodigo = TAMANHO_INICIAL_DICIONARIO;
            StringBuilder palavra = new StringBuilder();
            List<String> dadosSaida = new ArrayList<>();

            int codigoAnterior = dis.readShort();
            palavra.append(dicionario.get(codigoAnterior));
            dadosSaida.add(dicionario.get(codigoAnterior));
            int codigo;
            while (dis.available() > 0) {
                codigo = dis.readShort();
                String entrada;
                if (dicionario.containsKey(codigo)) {
                    entrada = dicionario.get(codigo);
                } else if (codigo == proximoCodigo) {
                    entrada = palavra.toString() + palavra.charAt(0);
                } else {
                    throw new IllegalArgumentException("Código não encontrado no dicionário.");
                }

                dadosSaida.add(entrada);

                dicionario.put(proximoCodigo++, palavra.toString() + entrada.charAt(0));

                palavra = new StringBuilder(entrada);
            }

            for (String dado : dadosSaida) {
                for (char c : dado.toCharArray()) {
                    fos.write(c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] abrirArquivoComoBytes(String caminhoArquivo) throws IOException {
        Path caminho = Paths.get(caminhoArquivo);
        return Files.readAllBytes(caminho);
    }

    public void criarBackup(String caminhoArquivo, String dirBackup) throws IOException {
        byte[] dados = abrirArquivoComoBytes(caminhoArquivo);
        String nomeArquivo = Paths.get(caminhoArquivo).getFileName().toString();
        String dataAtual = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String caminhoBackup = dirBackup + "/" + dataAtual;
        new File(caminhoBackup).mkdirs();
        comprimirArquivo(Arrays.toString(dados), caminhoBackup + "/" + nomeArquivo + ".lzw");
    }

    public void recuperarBackup(String dirBackup, String versao, String caminhoArquivoSaida) throws IOException {
        String caminhoArquivoComprimido = dirBackup + "/" + versao + "/" + Paths.get(caminhoArquivoSaida).getFileName().toString() + ".lzw";
        descomprimirArquivo(caminhoArquivoComprimido, caminhoArquivoSaida);
    }

    private Map<String, Integer> inicializarDicionario() {
        Map<String, Integer> dicionario = new HashMap<>();
        for (int i = 0; i < TAMANHO_INICIAL_DICIONARIO; i++) {
            dicionario.put(String.valueOf((char) i), i);
        }
        return dicionario;
    }

    private Map<Integer, String> inicializarDicionarioReverso() {
        Map<Integer, String> dicionario = new HashMap<>();
        for (int i = 0; i < TAMANHO_INICIAL_DICIONARIO; i++) {
            dicionario.put(i, String.valueOf((char) i));
        }
        return dicionario;
    }
}