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
  public ArrayList<NonTerminal> nullables = new ArrayList<>();
  public ArrayList<Edge> inverseUnitRelations = new ArrayList<>();
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
              Gramatica.alfabeto.add(("" + variaveis.charAt(i)));
            }
          }
          // Obter terminais
          for (int i = 0; i < terminais.length(); i++) {
            if (verificar(terminais.charAt(i))) {
              this.terminais.add(("" + terminais.charAt(i)));
              Gramatica.alfabeto.add(("" + terminais.charAt(i)));
            }
          }
          // Obter inicial
          for (int i = 0; i < inicial.length(); i++) {
            if (verificar(inicial.charAt(i))) {
              this.inicial += ("" + inicial.charAt(i));
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
      this.nullables = this.getNullables();
      this.inverseUnitRelations = this.getInverseUnitRelation();
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

  public String toText() {
    String result = "Quantidade de regras: " + this.productions.size() + "\n";
    for (int i = 0; i < this.productions.size(); i++) {
      result += this.productions.get(i).toText() + "\n";
    }
    return result;
  }

  public ArrayList<NonTerminal> getNullables(){
    /*
     * retorna uma lista de todas as variaveis que podem produzir LAMBDA (diretamente ou indiretamente)
     */
    
    ArrayList<NonTerminal> nullables= new ArrayList<>();

    //encontra as variaveis que geram LAMBDA diretamente
    for (int i = 0; i < this.productions.size(); i++) {
      if(this.productions.get(i).result.equals("")){
        nullables.add(this.productions.get(i).nonTerminal);
      }
    }

    boolean nullablesChanged = false;
    //encontra todas as variaveis que geram LAMBDA indiretamente
    do {
      nullablesChanged = false;
      for (int i = 0; i < this.productions.size(); i++) {
        if(this.productions.get(i).containsOnlyFrom(nullables)){
          nullables.add(this.productions.get(i).nonTerminal);
          nullablesChanged=true;
        }
      }
    } while (nullablesChanged);

    return nullables;
  }

  public ArrayList<Edge> getInverseUnitRelation(){
    /*
     * retorna uma lista de todos os pares ordenados (y, A) que significa que A->y (em 1 passo)
     */
    ArrayList<Edge> Ig = new ArrayList<>();
    ArrayList<String> allSimbols = new ArrayList<>();
    allSimbols.addAll(this.terminais);
    allSimbols.addAll(this.variaveis);

    for (int i = 0; i < this.productions.size(); i++) {
      String result = this.productions.get(i).result;

      //inclui (y, A) caso A->y ou A->By ou A->yB em que B pode gerar LAMBDA, e y é um simbolo qualquer
      if(result.length()>0 && result.length()<3){//só funciona no formato 2NF
        if(result.length()==1){//caso em que A->y
          Ig.add(new Edge(result, this.productions.get(i).nonTerminal.name));//add (y, A)
        }
        else{
          if(NonTerminal.listContains(this.nullables, ""+result.charAt(0))){//caso em que A->By, em que B pode gerar LAMBDA
            Ig.add(new Edge(""+result.charAt(1), this.productions.get(i).nonTerminal.name));//add (y, A)
          }
          else if(NonTerminal.listContains(this.nullables, ""+result.charAt(1))){//caso em que A->yB, em que B pode gerar LAMBDA
            Ig.add(new Edge(""+result.charAt(0), this.productions.get(i).nonTerminal.name));//add (y, A)
          }
        }
      }
      
    }
    return Ig;
  }

  public ArrayList<Edge> getInvUnitRelOf(String M){
    /*
     * Retorna uma lista de todos os pares ordenados (y, A) em que A->* y, e y é um símbolo da palavra M
     */
    ArrayList<Edge> inverseUofM = new ArrayList<>();

    for (int i = 0; i < M.length(); i++) {
      ArrayList<NonTerminal> listProducesPartOfM = new ArrayList<>();
      String lookingFor = ""+M.charAt(i);//lookingFor = y

      //encontra todas as variaveis que geram lookingFor diretamente
      for (int j = 0; j < this.inverseUnitRelations.size(); j++) {
        if(this.inverseUnitRelations.get(j).start.equals(lookingFor)){
          listProducesPartOfM.add(new NonTerminal(this.inverseUnitRelations.get(j).end));
        }
        else if(this.inverseUnitRelations.get(j).start.equals(M)){
          listProducesPartOfM.add(new NonTerminal(this.inverseUnitRelations.get(j).end));
        }
      }

      boolean isListChanged = false;
      //encontra todas as variaveis que geram variaveis de listProducesPartOfM
      do {
        isListChanged = false;
        for (int j = 0; j < this.productions.size(); j++) {
          String result = this.productions.get(i).result;//A->result

          if(result.length()>0 && result.length()<3){
            if(result.length()==1){
              if(NonTerminal.listContains(listProducesPartOfM, result)){//caso em que A->C, em que C esta na lista listProducesPartOfM
                listProducesPartOfM.add(this.productions.get(i).nonTerminal);
                isListChanged=true;
              }
            }
            else if(NonTerminal.listContains(this.nullables, ""+result.charAt(0))){
              if(NonTerminal.listContains(listProducesPartOfM, ""+result.charAt(1))){//caso em que A->BC, em que B pode gerar LAMBDA e C esta na lista listProducesPartOfM
                listProducesPartOfM.add(this.productions.get(i).nonTerminal);
                isListChanged=true;
              }
            }
            else if(NonTerminal.listContains(this.nullables, ""+result.charAt(1))){
              if(NonTerminal.listContains(listProducesPartOfM, ""+result.charAt(0))){//caso em que A->CB, em que B pode gerar LAMBDA e C esta na lista listProducesPartOfM
                listProducesPartOfM.add(this.productions.get(i).nonTerminal);
                isListChanged=true;
              }
            }
          }
        }
      } while (isListChanged);

      listProducesPartOfM = NonTerminal.removeDuplicatesFromList(listProducesPartOfM);
      for (int j = 0; j <listProducesPartOfM.size(); j++) {
        inverseUofM.add(new Edge(lookingFor, listProducesPartOfM.get(j).name));
      }
    }
    return inverseUofM;
  }

  public ArrayList<String> getAuxiliarTset(String word){
    /*
     * retorna o conjunto das variáveis que geram (em 1 passo) as partes que compoem word. Ou entao, a propria word se |word|=1
     */
    ArrayList<String> auxiliarTset = new ArrayList<>();
    if(word.length()>0 && word.length()<3){
      if(word.length()==1){
        auxiliarTset.add(word);
      }
      else{
        String left = ""+word.charAt(0);//word = Left+Right, left=Left
        String right = ""+word.charAt(1);//word = Left+Right, right=Right
        
        ArrayList<NonTerminal> possiblesLeftNonTerminal = new ArrayList<>();//contem as variaveis que podem gerar left
        ArrayList<Edge> Tset = this.getTset(left);
        for (int i = 0; i < Tset.size(); i++) {
          possiblesLeftNonTerminal.add(new NonTerminal(Tset.get(i).end));
        }

        ArrayList<NonTerminal> possiblesRightNonTerminal = new ArrayList<>();//contem as variaveis que podem gerar right
        Tset = this.getTset(right);
        for (int i = 0; i < Tset.size(); i++) {
          possiblesRightNonTerminal.add(new NonTerminal(Tset.get(i).end));
        }

        if(possiblesLeftNonTerminal.size()>0 && possiblesRightNonTerminal.size()>0){
          for (int i = 0; i < possiblesLeftNonTerminal.size(); i++) {
            for (int j = 0; j < possiblesRightNonTerminal.size(); j++) {

              //verifica se existe alguma variavel A tal que A->LEFT+RIGHT, em que LEFT esta em possiblesLeftNonTerminal, e RIGHT esta em possiblesRightNonTerminal
              String desiredResult = possiblesLeftNonTerminal.get(i).name+possiblesRightNonTerminal.get(j).name;
              
              for (int k = 0; k < this.inverseUnitRelations.size(); k++) {
                if(this.inverseUnitRelations.get(k).end.equals(desiredResult)){
                  auxiliarTset.add(this.inverseUnitRelations.get(k).start);
                }
              }
            }
          }
        }
      }
    }
    return auxiliarTset;
  }

  public ArrayList<Edge> getTset(String word){
    /*
     * retorna uma lista de todos os pares ordenados (y, A) em que A->y, em que y é uma parte de word. Ou seja, retorna todas as variaveis que podem gerar (diretamente ou indiretamente) as partes de word
     */   
    ArrayList<String> Taux = this.getAuxiliarTset(word);
    String nonTerminals = "";
    
    for (int i = 0; i < Taux.size(); i++) {
      nonTerminals+=Taux.get(i);
    }
    
    return this.getInvUnitRelOf(nonTerminals);
  }

  public boolean producesWord(String word){
    if(this.inverseUnitRelations.size()<1){
      System.out.println("Tente inicializar a variavel antes de chamar o metodo CYK modificado");
      return false;
    }
    else{
      ArrayList<Edge> nonTerminals = this.getTset(word);
      for (int i = 0; i < nonTerminals.size(); i++) {
        if(nonTerminals.get(i).end.equals(this.initial.name)){
          return true;
        }
      }

      return false;
    }
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
      String novoNome = "";
      if(Gramatica.eTerminal(valor, g)) {
        boolean encontrado = false;
        for (int j = 0; j < possiveisNomes.length() && !encontrado; j++) {
          boolean exists = false;
          for (int k = 0; k < Gramatica.alfabeto.size() && !exists; k++) {
            if ((Gramatica.alfabeto.get(k)).equals("" + possiveisNomes.charAt(j))) {
              exists = true;
            }
          }

          if (!exists) {
            NonTerminal n = new NonTerminal("" + possiveisNomes.charAt(j));
            tempProductions.get(i).nonTerminal = n;
            novoNome = tempProductions.get(i).nonTerminal.name;
            Gramatica.alfabeto.add(tempProductions.get(i).nonTerminal.name);
            encontrado = true;
          }
        }

        for(int j = 0; j < tempProductions.size(); j++) {
          if(tempProductions.get(j).result.length() > 1) {
            for(int k = 0; k < tempProductions.get(j).result.length(); k++) {
              if((""+tempProductions.get(j).result.charAt(k)).equals(valor)) {
                if(k+1 < tempProductions.get(j).result.length()) {
                  tempProductions.get(j).result = tempProductions.get(j).result.substring(0, k) +
                                                  novoNome +
                                                  tempProductions.get(j).result.substring(k+1);
                } else {
                  tempProductions.get(j).result = tempProductions.get(j).result.substring(0, k) +
                                                  novoNome;
                }
              }
            }
          }
        }
      }
    }

    for(int i = 0; i < tempProductions.size(); i++) {
      for(int l = 0; l < tempProductions.get(i).result.length(); l++) {
        String valor = ""+tempProductions.get(i).result.charAt(l);
        String novoNome = "";

        if(Gramatica.eTerminal(valor, g)) {
          boolean encontrado = false;
          for (int j = 0; j < possiveisNomes.length() && !encontrado; j++) {
            boolean exists = false;
            for (int k = 0; k < Gramatica.alfabeto.size() && !exists; k++) {
              if ((Gramatica.alfabeto.get(k)).equals("" + possiveisNomes.charAt(j))) {
                exists = true;
              }
            }

            if (!exists) {
              NonTerminal n = new NonTerminal("" + possiveisNomes.charAt(j));
              novoNome = n.name;
              System.out.println(n.name+"->"+valor);
              tempProductions.add(new Production(n, valor));
              Gramatica.alfabeto.add(n.name);
              encontrado = true;
            }
          }

          for(int j = 0; j < tempProductions.size(); j++) {
            if(tempProductions.get(j).result.length() > 1) {
              for(int k = 0; k < tempProductions.get(j).result.length(); k++) {
                if((""+tempProductions.get(j).result.charAt(k)).equals(valor)) {
                  if(k+1 < tempProductions.get(j).result.length()) {
                    tempProductions.get(j).result = tempProductions.get(j).result.substring(0, k) +
                                                    novoNome +
                                                    tempProductions.get(j).result.substring(k+1);
                  } else {
                    tempProductions.get(j).result = tempProductions.get(j).result.substring(0, k) +
                                                    novoNome;
                  }
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

   public boolean containsOnlyFrom(ArrayList<NonTerminal> listFrom){
    ArrayList<String> listFromNames = new ArrayList<>();
    
    listFrom.forEach((p)->{
      listFromNames.add(p.name);
    });

    for (int i = 0; i < this.result.length(); i++) {
      if(listFromNames.contains(""+this.result.charAt(i))==false){//se alguma das variaveis de this.result não estiver em listFrom, retorna false
        return false;
      }
    }
    return true;
  }
}

class NonTerminal {
  public String name = "";

  public NonTerminal(String name) {
    this.name = name;
    Gramatica.alfabeto.add(this.name);
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

  public static boolean listContains(ArrayList<NonTerminal> list, String value){
    for (int i = 0; i < list.size(); i++) {
      if(list.get(i).name.equals(value)){
        return true;
      }
    }
    return false;
  }

  public static ArrayList<NonTerminal> removeDuplicatesFromList(ArrayList<NonTerminal> list){
    ArrayList<NonTerminal> noDuplicates = new ArrayList<>();
    list.forEach((element)->{
      if(noDuplicates.contains(element)==false){
        noDuplicates.add(element);
      }
    });
    return noDuplicates;
  }
}

class Edge{
  public String start;
  public String end;


  public Edge(String start, String end){
    this.start=start;
    this.end=end;
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
            Gramatica gAuxiliar = Gramatica.convertTo2NF(g);
            Gramatica.convertToCNF(g);
            g.initial = gAuxiliar.initial;
            g.productions = gAuxiliar.productions;
          }
          break;
        case 4:
          if(g != null) {
            for (int i = 0; i < g.sequancias.size(); i++) {
              boolean answer = g.producesWord(g.sequancias.get(i));
              if(answer){
                System.out.println("A sentenca "+g.sequancias.get(i)+" pertence (SIM) a gramatica!");
              }
              else{
                System.out.println("A sentenca "+g.sequancias.get(i)+" NAO pertence a gramatica!");
              }
            }
          }
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
