/*
* Trabalho de FTC
*
*
* --- Funcionalidades do Código ---
* 1) Ler um arquivo e criar um objeto Gramatica
* 2) Transformar uma gramatica na forma 2NF
* 3) Transformar uma gramatica na forma de Chomsky (CFN)
* 4) Aplicar o algoritmo CYK (original-utilizando CFN) para detectar se uma
* palavra esta na gramatica
* 5) Aplicar o algoritmo CYK (modificado-utilizando 2FN) para detectar se uma
* palavra esta na gramatica
* 6) Retornar se a sentença está na gramática
*/

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Gramatica {
  ArrayList<String> variaveis = new ArrayList<>();
  ArrayList<String> terminais = new ArrayList<>();
  ArrayList<String> regras = new ArrayList<>();
  ArrayList<String> sequancias = new ArrayList<>();
  String inicial = "";

  private static boolean verificar(char caracter) {
    return (caracter != ' ' && caracter != ','
         && caracter != '{' && caracter != '('? true : false);
  }

  public void ParaString() throws Exception {
    System.out.println("\n\n\t (Gramatica)");
    System.out.println("\nVariaveis: ");
    for(int i = 0; i < this.variaveis.size(); i++) {
      System.out.println(this.variaveis.get(i));
    }
    System.out.println("\nTerminais: ");
    for(int i = 0; i < this.terminais.size(); i++) {
      System.out.println(this.terminais.get(i));
    }
    System.out.println("\nRegras: ");
    for(int i = 0; i < this.regras.size(); i++) {
      System.out.println(this.regras.get(i));
    }
    System.out.println("\nInicial: ");
    System.out.println(this.inicial);
    System.out.println("\nSequancias: ");
    for(int i = 0; i < this.sequancias.size(); i++) {
      System.out.println(this.sequancias.get(i));
    }
  }

  Gramatica() {
    try {
      Scanner leitor = new Scanner(new FileReader("./entrada.txt"));
      boolean primeiraLinha = true;
      int quantidadeRegras = 0;

      while(leitor.hasNextLine()) {
        String linha = leitor.nextLine();

        if (primeiraLinha) {
          String auxiliar, variaveis, terminais, regras, inicial, regra = "";

          // Obter regioes de valores
          variaveis = linha.substring(0, linha.indexOf("}"));
          auxiliar = linha.substring(linha.indexOf("}")+1);
          terminais = auxiliar.substring(0, auxiliar.indexOf("}"));
          auxiliar = auxiliar.substring(auxiliar.indexOf("}")+1);
          regras = auxiliar.substring(0, auxiliar.indexOf("}"));
          auxiliar = auxiliar.substring(auxiliar.indexOf("}")+1);
          inicial = auxiliar.substring(0, auxiliar.indexOf(")"));

          // Obter variaveis
          for(int i = 0; i < variaveis.length(); i++) {
            if(verificar(variaveis.charAt(i))) {
              this.variaveis.add((""+variaveis.charAt(i)));
            }
          }
          // Obter terminais
          for(int i = 0; i < terminais.length(); i++) {
            if(verificar(terminais.charAt(i))) {
              this.terminais.add((""+terminais.charAt(i)));
            }
          }
          // Obter inicial
          for(int i = 0; i < inicial.length(); i++) {
            if(verificar(inicial.charAt(i))) {
              this.inicial += ((""+inicial.charAt(i)));
            }
          }
          // Obter regras
          for(int i = 0; i < regras.length(); i++) {
            if(regras.charAt(i) == ',') {
              quantidadeRegras++;
            }
          }

          auxiliar = regras.substring(regras.indexOf(",")+1);
          regras = auxiliar.substring(0, auxiliar.indexOf(","));
          for(int i = 0; i < quantidadeRegras; i++) {
            if(auxiliar.indexOf(",") != -1) {
              regras = auxiliar.substring(0, auxiliar.indexOf(","));
            } else {
              regras = auxiliar;
            }

            for(int j = 0; j < regras.length(); j++) {
              if(verificar(regras.charAt(j))) {
                regra += regras.charAt(j);
              }
            }

            this.regras.add(regra);

            if(auxiliar.indexOf(",") != -1) {
              auxiliar = auxiliar.substring(auxiliar.indexOf(",")+1);
            }
            regra = "";
          }
          primeiraLinha = false;
        } else {
          sequancias.add(linha);
        }
      }
    } catch (Exception e) {
      System.out.println("---\n\tERRO: Certifiquece que o arquivo (entrada.txt)"+
                         "\n\tse encontra no mesmo diretorio do codigo e se a"+
                         "\n\tentrada esta da forma recomendada. \n---");
    }
  }


}

class Main {
  public static void limparTerminal() throws Exception {
    if (System.getProperty("os.name").contains("Windows")) {
      new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
    } else {
      Runtime.getRuntime().exec("clear");
    }
  }

  public static void main(String[] args) throws Exception{
    // Variaveis
    Scanner leitor = new Scanner(System.in);
    int opcao = -1;

    // Inicio do programa
    while(opcao != 0) {
      limparTerminal();
      System.out.println("Trabalho de FTC - Ciencia da Computacao - 2022");
      System.out.println("0) Fechar programa.");
      System.out.println("1) Algoritmo CYK original");
      System.out.println("2) Algoritmo CYK modificado");
      System.out.print("Escolha uma opcao: ");

      opcao = leitor.nextInt();

      switch(opcao) {
        case 1:
          Gramatica g = new Gramatica();
          g.ParaString();
          break;
        case 2:
          break;
        default:
          limparTerminal();
          System.out.println("---\n\tERRO: Opcao invalida. \n---");
      }

      if (opcao != 0) {
        System.out.print("\nPrecione Entrer para continuar... ");
        System.in.read();
      }

      limparTerminal();
    }

  }
}
