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
  public ArrayList<String> variaveis = new ArrayList<>();
  public NonTerminal initial;
  public String inicial = "";
  public ArrayList<Production> productions = new ArrayList<>();
  public ArrayList<String> regras = new ArrayList<>();
  public ArrayList<String> terminais = new ArrayList<>();
  public ArrayList<String> sequancias = new ArrayList<>();
  public static ArrayList<String> alfabeto = new ArrayList<>();

  Gramatica() {
    try {
      Scanner leitor = new Scanner(new FileReader("./entrada.txt"));
      boolean primeiraLinha = true;
      int quantidadeRegras = 0;

      while (leitor.hasNextLine()) {
        String linha = leitor.nextLine();

        if (primeiraLinha) {
          String auxiliar, variaveis, terminais, regras, inicial, regra = "";

          // Obter regioes de valores
          variaveis = linha.substring(0, linha.indexOf("}"));
          auxiliar = linha.substring(linha.indexOf("}") + 1);
          terminais = auxiliar.substring(0, auxiliar.indexOf("}"));
          auxiliar = auxiliar.substring(auxiliar.indexOf("}") + 1);
          regras = auxiliar.substring(0, auxiliar.indexOf("}"));
          auxiliar = auxiliar.substring(auxiliar.indexOf("}") + 1);
          inicial = auxiliar.substring(0, auxiliar.indexOf(")"));

          // Obter variaveis
          for (int i = 0; i < variaveis.length(); i++) {
            if (verificar(variaveis.charAt(i))) {
              this.variaveis.add(("" + variaveis.charAt(i)));
            }
          }
          // Obter terminais
          for (int i = 0; i < terminais.length(); i++) {
            if (verificar(terminais.charAt(i))) {
              this.terminais.add(("" + terminais.charAt(i)));
            }
          }
          // Obter inicial
          for (int i = 0; i < inicial.length(); i++) {
            if (verificar(inicial.charAt(i))) {
              this.inicial += (("" + inicial.charAt(i)));
            }
          }
          // Obter regras
          for (int i = 0; i < regras.length(); i++) {
            if (regras.charAt(i) == ',') {
              quantidadeRegras++;
            }
          }

          auxiliar = regras.substring(regras.indexOf(",") + 1);
          regras = auxiliar.substring(0, auxiliar.indexOf(","));
          for (int i = 0; i < quantidadeRegras; i++) {
            if (auxiliar.indexOf(",") != -1) {
              regras = auxiliar.substring(0, auxiliar.indexOf(","));
            } else {
              regras = auxiliar;
            }

            for (int j = 0; j < regras.length(); j++) {
              if (verificar(regras.charAt(j))) {
                regra += regras.charAt(j);
              }
            }

            this.regras.add(regra);
            String line = regra;
            String[] vet1 = line.split("->");
            NonTerminal atual = new NonTerminal(vet1[0]);
            String[] vet2 = vet1[1].split("\\|");
            for (int j = 0; j < vet2.length; j++) {
              Production newP = new Production(atual, vet2[j]);
              this.productions.add(newP);
            }

            if (auxiliar.indexOf(",") != -1) {
              auxiliar = auxiliar.substring(auxiliar.indexOf(",") + 1);
            }
            regra = "";
          }
          primeiraLinha = false;
        } else {
          sequancias.add(linha);
        }
      }
      this.initial = new NonTerminal(this.inicial);
    } catch (Exception e) {
      System.out.println("---\n\tERRO: Certifiquece que o arquivo (entrada.txt)" +
          "\n\tse encontra no mesmo diretorio do codigo e se a" +
          "\n\tentrada esta da forma recomendada. \n---");
    }
  }

  public Gramatica(boolean valor) {
    this.initial = new NonTerminal("");
    this.productions = new ArrayList<Production>();
  }

  private static boolean verificar(char caracter) {
    return (caracter != ' ' && caracter != ','
        && caracter != '{' && caracter != '(' ? true : false);
  }

  public static boolean eTerminal(String v, Gramatica g) throws Exception {
    for(int i = 0; i < g.terminais.size(); i++) {
      if(g.terminais.get(i).equals(v)) {
        return true;
      }
    }
    return false;
  }

  public void ParaString() throws Exception {
    System.out.println("\n\n\t (Gramatica)");

    System.out.println("\nVariaveis: ");
    for (int i = 0; i < this.variaveis.size(); i++) {
      System.out.println(this.variaveis.get(i));
    }

    System.out.println("\nTerminais: ");
    for (int i = 0; i < this.terminais.size(); i++) {
      System.out.println(this.terminais.get(i));
    }

    System.out.println("\nRegras: ");
    for (int i = 0; i < this.regras.size(); i++) {
      System.out.println(this.regras.get(i));
    }

    System.out.println("\nTransformacoes: ");
    for (int i = 0; i < this.productions.size(); i++) {
      System.out.println(this.productions.get(i).toText());
    }

    System.out.println("\nInicial: ");
    System.out.println(this.inicial);

    System.out.println("\nSequancias: ");
    for (int i = 0; i < this.sequancias.size(); i++) {
      System.out.println(this.sequancias.get(i));
    }
  }

  public static Gramatica convertTo2NF(Gramatica grammar) {
    Gramatica converted = new Gramatica(true);
    grammar.productions.forEach((p) -> {
      if (p.size > 2) {
        ArrayList<Production> newProductions = Production.convertProdutionTo2NF(p);
        converted.productions.addAll(newProductions);
      } else {
        converted.productions.add(p);
      }
    });
    return converted;
  }

  public static void convertToCNF(Gramatica g) throws Exception {
    Gramatica converted = new Gramatica(true);
    Production.convertProdutionToCNF(g.productions, g);
  }
}

class Production {
  public NonTerminal nonTerminal;
  public String result;
  public int size = 0;

  public Production(NonTerminal v, String result) {
    this.nonTerminal = v;
    this.result = result;
    this.size = result.length();
  }

  public String getLeft() {
    return "" + this.result.charAt(0);
  }

  public String getRight() {
    String left = "";
    for (int i = 1; i < this.result.length(); i++) {
      left = left + this.result.charAt(i);
    }
    return left;
  }


  public static void convertProdutionToCNF(ArrayList<Production> p, Gramatica g) throws Exception {
    String possiveisNomes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ/,.\\*&%$#@!?";
    ArrayList<Production> tempProductions = new ArrayList<Production>();

    tempProductions.addAll(p);
    for(int i = 0; i < tempProductions.size(); i++) {
      String valor = tempProductions.get(i).result;
      if(Gramatica.eTerminal(valor, g)) {
        for (int j = 0; j < possiveisNomes.length(); j++) {
          boolean exists = false;
          for (int k = 0; k < Gramatica.alfabeto.size(); k++) {
            if ((Gramatica.alfabeto.get(k)).equals("" + possiveisNomes.charAt(j))) {
              exists = true;
            }
          }

          if (!exists) {
            tempProductions.get(i).nonTerminal.name = "" + possiveisNomes.charAt(i);
            Gramatica.alfabeto.add(tempProductions.get(i).nonTerminal.name);
          }
        }

        for(int j = 0; j < tempProductions.size(); j++) {
          if(tempProductions.get(j).result.length() > 1) {
            for(int k = 0; k < tempProductions.get(k).result.length(); k++) {
              if((""+tempProductions.get(k).result.charAt(k)).equals(valor)) {
                if(k+1 < tempProductions.get(k).result.length()) {
                  tempProductions.get(k).result = tempProductions.get(k).result.substring(0, k) +
                    tempProductions.get(i).nonTerminal.name.charAt(0) +
                         tempProductions.get(k).result.substring(k+1);
                } else {
                  tempProductions.get(k).result = tempProductions.get(k).result.substring(0, k) +
                    tempProductions.get(i).nonTerminal.name.charAt(0);
                }
              }
            }
          }
        }
      }
    }

    g.productions.addAll(tempProductions);
  }

  public static ArrayList<Production> convertProdutionTo2NF(Production p) {
    ArrayList<Production> newProductions = new ArrayList<Production>();
    if (p.size > 2) {
      String left = p.getLeft();
      NonTerminal newV = new NonTerminal();
      Production newGenericProduction = new Production(newV, p.getRight());
      Production changeP = new Production(p.nonTerminal, left + newV.name);
      newProductions.add(changeP);
      newProductions.addAll(convertProdutionTo2NF(newGenericProduction));
    } else {
      newProductions.add(p);
    }
    return newProductions;
  }

  public String toText() {
    String production = this.nonTerminal.name + "->" + this.result;
    return production;
  }
}

class NonTerminal {
  public String name = "";

  public NonTerminal(String name) {
    this.name = name;
  }

  public NonTerminal() {
    String possiveisNomes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ/,.\\*&%$#@!?";
    for (int i = 0; i < possiveisNomes.length(); i++) {
      boolean exists = false;
      for (int j = 0; j < Gramatica.alfabeto.size(); j++) {
        if ((Gramatica.alfabeto.get(j)).equals("" + possiveisNomes.charAt(i))) {
          exists = true;
        }
      }
      if (!exists) {
        this.name = "" + possiveisNomes.charAt(i);
        Gramatica.alfabeto.add(this.name);
        break;
      }
    }
  }
}

public class Main {
  public static void limparTerminal() throws Exception {
    if (System.getProperty("os.name").contains("Windows")) {
      new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
    } else {
      Runtime.getRuntime().exec("clear");
    }
  }

  public static void main(String[] args) throws Exception {
    // Variaveis
    Scanner leitor = new Scanner(System.in);
    int opcao = -1;
    Gramatica g = null;
    // Inicio do programa
    while (opcao != 0) {
      limparTerminal();
      System.out.println("Trabalho de FTC - Ciencia da Computacao - 2022");
      System.out.println("0) Fechar programa.");
      System.out.println("1) Ler arquivo Entrada");
      System.out.println("2) Mostrar gramatica");
      System.out.println("3) Algoritmo CYK original");
      System.out.println("4) Algoritmo CYK modificado");
      System.out.print("Escolha uma opcao: ");

      opcao = leitor.nextInt();
      switch (opcao) {
        case 1:
          g = new Gramatica();
          g.ParaString();
          break;
        case 2:
          if(g != null) {
            g.ParaString();
          }
          break;
        case 3:
          if(g != null) {
            Gramatica gAuxiliar = g.convertTo2NF(g);
            Gramatica.convertToCNF(g);
            g.initial = gAuxiliar.initial;
            g.productions = gAuxiliar.productions;
          }
          break;
        case 4:
          if(g != null) {}
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

    leitor.close();
  }
}
