package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class homework {

    public static class Section{    //store in hashset
        int num, area;
        public Section(int a, int b){
            num = a;
            area = b;
        }

        public boolean equals(Object obj) {
            Section s = (Section) obj;
            return this.num == s.num && this.area == s.area;
        }

        @Override
        public int hashCode() {
            final int prime = 31;//
            //int result = prime * num + area;

            return prime * num + area;
        }
    }

    public static class Node{
        int x, y, dR, dL, areaX, areaY, areaDR, areaDL;
        public Node(int a, int b,int[][] grid){
            int n = grid.length;
            x = a;                  // 4 direction coordinates
            y = b;
            dR = a + b;
            dL = n - b + a;
            areaX = 0;
            for(int i = 1; i < y; i++){
                if(grid[x][i] == 2 && grid[x][i - 1] == 0){
                    areaX++;
                }
            }
            areaY = 0;
            for(int i = 1; i < x; i++){
                if(grid[i][y] == 2 && grid[i - 1][y] == 0){
                    areaY++;
                }
            }
            areaDR = 0;
            for(int i = 1; i < Math.min(x,n - y - 1); i++){
                if(y < n - 1 && x > 1 && grid[x - i][y + i] == 2 && grid[x - i - 1][y + i + 1] == 0){
                    areaDR++;
                }
            }
            areaDL = 0;
            for(int i = 1; i < Math.min(x,y); i++){
                if(x > 1 && y > 1 && grid[x - i][y - i] == 2 && grid[x - i - 1][y - i - 1] == 0){
                    areaDL++;
                }
            }

        }
        public boolean equals(Object obj) {
            Node s = (Node) obj;
            return this.x == s.x && this.y == s.y;
        }

        @Override
        public int hashCode() {
            final int prime = 31;//
            int result = prime * x + y;

            return result;
        }
    }


    public static void main(String[] args) {
	// write your code here
        File file = new File("input");

        int number = 0;
        int n = 0;
        int[][] grid = new int[n][n];
        String method = null;
        try {

            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                method = sc.nextLine();
                if(sc.hasNext()){
                    n = sc.nextInt();
                }else{
                    break;
                }

                number = sc.nextInt();
                sc.nextLine();
                grid = new int[n][n];
                //System.out.println("" + method + n + number);
                for(int i = 0; i < n; i++){
                    String str = sc.nextLine();
                    //System.out.println(str);
                    for(int j = 0; j < n; j++){
                        if('2' == (str.charAt(j))){
                            grid[i][j] = 2;
                        }else {
                            grid[i][j] = 0;
                        }
                        //System.out.println(grid[i][j]);
                    }
                }
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        number = 10;
//        grid = new int[10000][10000];
        ArrayList<Node> result = new ArrayList<>();

        if(method.equals("DFS")){
            HashSet<Section> setX = new HashSet<>();
            HashSet<Section> setY = new HashSet<>();
            HashSet<Section> setDR = new HashSet<>();
            HashSet<Section> setDL = new HashSet<>();

            dfs(0, 0, grid, number, result, setX, setY, setDR, setDL);
        }else if(method.equals("BFS")){
            Queue<Node[]> queue = new LinkedList<>();
            Node[] list;
            int count = 0;
            for(int i = 0; i < n; i++){
                for(int j = 0; j < n; j++){
                    if(count > n){break;}
                    if(grid[i][j] == 0){
                        list = new Node[1];
                        list[0] = new Node(i, j, grid);
                        queue.offer(list);
                        count++;
                    }
                }
            }
            bfs(grid, number, queue);
        }else if (method.equals("SA")){
            int temperature = number * number;
            simulatedAnnealing(grid, temperature, result, number);
        }
    }

    private static boolean dfs(int x, int y, int[][] grid, int number, ArrayList<Node> result,
                            HashSet<Section> setX, HashSet<Section> setY, HashSet<Section> setDR, HashSet<Section> setDL){
        int len = grid.length;
        if(result.size() == number){
            try{
                File file = new File("output.txt");
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println("OK");
                int count =0;
                for(int i = 0; i < len; i++){
                    for(int j = 0; j < len; j++){
                        if(count < number && result.get(count).x == i && result.get(count).y == j){
                            writer.print("1");
                            count++;
                        }else if(grid[i][j] == 2){
                            writer.print("2");
                        }else{
                            writer.print("0");
                        }
                    }
                    writer.println();
                }

                writer.close();
            } catch (IOException e) {
                // do something
            }

            return true;
        }

        for(int i = x; i < len; i++){
            if(i > x) y = 0;
            for(int j = y; j < len; j++){
                Node curr = new Node(i, j ,grid);
                if(grid[i][j] == 2 || !check(setX, setY, setDR, setDL, curr)){
                    continue;                                               //if there is tree or under restriction
                }
                result.add(curr);
                updateSection(setX, setY, setDR, setDL, curr);              //add restriction

                if(dfs(i, j, grid, number , result,  setX, setY, setDR, setDL)) return true;

                Node dead = result.remove(result.size() - 1);
                deleteSection(setX, setY, setDR, setDL, dead);              //remove the dead node's restriction
            }
        }

        try{
            File file = new File("output.txt");
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println("FAIL");

            writer.close();
        } catch (IOException e) {
            // do something
        }
        return false;
    }

    private static void bfs(int[][] grid, int number, Queue<Node[]> queue){
        int len = grid.length;
        Node[] curr;
        HashSet<Section> setX;
        HashSet<Section> setY;
        HashSet<Section> setDR;
        HashSet<Section> setDL;
        while(!queue.isEmpty()){
            curr = queue.poll();
            if(curr.length == number){
                try{
                    File file = new File("output.txt");
                    PrintWriter writer = new PrintWriter(file, "UTF-8");
                    writer.println("OK");
                    int count =0;
                    for(int i = 0; i < len; i++){
                        for(int j = 0; j < len; j++){
                            if(count < number && curr[count].x == i && curr[count].y == j){
                                writer.print("1");
                                count++;
                            }else if(grid[i][j] == 2){
                                writer.print("2");
                            }else{
                                writer.print("0");
                            }
                        }
                        writer.println();
                    }

                    writer.close();
                } catch (IOException e) {
                    // do something
                }
                return;
            }

            setX = new HashSet<>();
            setY = new HashSet<>();
            setDR = new HashSet<>();
            setDL = new HashSet<>();

            for(int i = 0; i < curr.length; i++){
                updateSection(setX, setY, setDR, setDL, curr[i]);
            }

            int last = curr[curr.length - 1].x;
            Node node;
            int count = 0;

            for(int i = last; i < len; i++){
                for(int j = 0; j < len; j++){
                    if(count > len){
                        break;
                    }
                    node = new Node(i, j, grid);
                    if(grid[i][j] == 2 || !check(setX, setY, setDR, setDL, node)){
                        continue;                                               //if there is tree or under restriction
                    }
                    Node[] newList = new Node[curr.length + 1];
                    System.arraycopy(curr,0, newList, 0, curr.length);  //copy array
                    newList[newList.length - 1] = node;                                 //add the new node
                    queue.offer(newList);
                    count++;
                }
            }
        }

        try{
            File file = new File("output.txt");
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println("FAIL");

            writer.close();
        } catch (IOException e) {
            // do something
        }
        //System.out.print("FAIL");
    }

    private static void simulatedAnnealing(int[][] grid, int temperatureSet, ArrayList<Node> result, int number){
        int len = grid.length;
        int numZero = 0;
        for(int i = 0; i < len; i++){
            for(int j = 0; j < len;j++){
                if(grid[i][j] == 0){
                    numZero++;
                }
            }
        }
        if(numZero < number) {
            try{
                File file = new File("output.txt");
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println("FAIL");

                writer.close();
            } catch (IOException e) {
                // do something
            }
            return;
        }

        Random randomGenerator = new Random();      //randomly generate a solution
        HashSet<Node> set = new HashSet<>();
        int x,y;
        while(result.size() != number){
            x = randomGenerator.nextInt(len);
            y = randomGenerator.nextInt(len);
            Node newNode = new Node(x,y,grid);
            if(grid[x][y] != 2 && !set.contains(newNode)){
                result.add(newNode);
                set.add(newNode);
            }
        }

        int numConflict = checkConflict(result);

        double temperature = temperatureSet;
        int k;
        for(k = 0; k < 100000; k++){

            double delta;
            double probability;
            double rand;
            ArrayList<Node> nextResult;
            int nextConflict;

            //for (int m = 500; (m > 0) && (numConflict != 0); m--) {
            if(numConflict == 0){
                break;
            }
            nextResult = getNextResult(result, grid, randomGenerator, number, set);
            set.clear();
            //Collections.addAll(nextResult);
//            for(int i = 0; i < nextResult.size(); i++){
//                set.add(nextResult.get(i));
//            }
            
            nextConflict = checkConflict(nextResult);

            delta = numConflict - nextConflict;
            probability = Math.exp(delta / temperature);
            rand = Math.random();

            if (delta > 0) {                    //if nextResult is better, choose it
                result = nextResult;
                numConflict = nextConflict;
                //System.out.println(numConflict);
            } else if (rand <= probability) {
                result = nextResult;
                numConflict = nextConflict;
            }

            //}
            temperature = temperature * 0.99;
        }

        if(numConflict == 0){
            //System.out.println(k);
            Collections.sort(result, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    if(o1.x != o2.x){
                        return o1.x - o2.x;
                    }else{
                        return o1.y - o2.y;
                    }
                }
            });
            try{
                File file = new File("output.txt");
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println("OK");
                int count =0;
                for(int i = 0; i < len; i++){
                    for(int j = 0; j < len; j++){
                        if(count < number && result.get(count).x == i && result.get(count).y == j){
                            writer.print("1");
                            count++;
                        }else if(grid[i][j] == 2){
                            writer.print("2");
                        }else{
                            writer.print("0");
                        }
                    }
                    writer.println();
                }

                writer.close();
            } catch (IOException e) {
                // do something
            }
            return;
        }else{
            try{
                File file = new File("output.txt");
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println("FAIL");

                writer.close();
            } catch (IOException e) {
                // do something
            }
            //System.out.println("False");
            return;
        }
    }


    private static void updateSection(HashSet<Section> setX, HashSet<Section> setY, HashSet<Section> setDR, HashSet<Section> setDL, Node curr){
        setX.add(new Section(curr.x, curr.areaX));
        setY.add(new Section(curr.y, curr.areaY));
        setDR.add(new Section(curr.dR, curr.areaDR));
        setDL.add(new Section(curr.dL, curr.areaDL));

    }

    private  static void deleteSection(HashSet<Section> setX, HashSet<Section> setY, HashSet<Section> setDR, HashSet<Section> setDL, Node dead){
        setX.remove(new Section(dead.x, dead.areaX));
        setY.remove(new Section(dead.y, dead.areaY));
        setDR.remove(new Section(dead.dR, dead.areaDR));
        setDL.remove(new Section(dead.dL, dead.areaDL));
    }

    private static boolean check(HashSet<Section> setX, HashSet<Section> setY, HashSet<Section> setDR, HashSet<Section> setDL, Node curr){
        if(setX.contains(new Section(curr.x, curr.areaX)) ||
                setY.contains(new Section(curr.y, curr.areaY)) ||
                setDR.contains(new Section(curr.dR, curr.areaDR)) ||
                setDL.contains(new Section(curr.dL, curr.areaDL))){
            return false;
        }else{
            return true;
        }
    }

    private static int getNumValid(ArrayList<Node> result){
        HashSet<Section> setX = new HashSet<>();
        HashSet<Section> setY = new HashSet<>();
        HashSet<Section> setDR = new HashSet<>();
        HashSet<Section> setDL = new HashSet<>();

        Node curr;
        int numValid = 0;                           //find the number of valid node
        for(int i = 0; i < result.size(); i++){
            curr = result.get(i);
            if(check(setX, setY, setDR, setDL, curr)){
                numValid++;
                updateSection(setX, setY, setDR, setDL, curr);
            }
        }

        return numValid;
    }

    private static ArrayList<Node> getNextResult(ArrayList<Node> result, int[][] grid, Random randomGenerator, int number, HashSet<Node> set){
        ArrayList<Node> newResult = new ArrayList<>(result);
        int len = grid.length;
        int index = randomGenerator.nextInt(number);
        int x, y;
        x = randomGenerator.nextInt(len);
        y = randomGenerator.nextInt(len);

        while((grid[x][y] == 2) || set.contains(new Node(x,y,grid))){
            x = randomGenerator.nextInt(len);
            y = randomGenerator.nextInt(len);
        }
        Node newNode = new Node(x, y, grid);
        newResult.set(index, newNode);
        return newResult;
    }

    private static int checkConflict(ArrayList<Node> result){
        int numConflict = 0;
        HashMap<Section, Integer> hashMapX = new HashMap<>();
        HashMap<Section, Integer> hashMapY = new HashMap<>();
        HashMap<Section, Integer> hashMapDR = new HashMap<>();
        HashMap<Section, Integer> hashMapDL = new HashMap<>();

        for(int i = 0; i < result.size(); i++){
            Node curr = result.get(i);
            Section sectionX = new Section(curr.x, curr.areaX);
            if(!hashMapX.containsKey(sectionX)){
                hashMapX.put(sectionX, 1);
            }else{
                numConflict = numConflict + hashMapX.get(sectionX);
                hashMapX.put(sectionX, hashMapX.get(sectionX) + 1);
            }

            Section sectionY = new Section(curr.y, curr.areaY);
            if(!hashMapY.containsKey(sectionY)){
                hashMapY.put(sectionY, 1);
            }else{
                numConflict = numConflict + hashMapY.get(sectionY);
                hashMapY.put(sectionY, hashMapY.get(sectionY) + 1);
            }

            Section sectionDR = new Section(curr.dR, curr.areaDR);
            if(!hashMapDR.containsKey(sectionDR)){
                hashMapDR.put(sectionDR, 1);
            }else{
                numConflict = numConflict + hashMapDR.get(sectionDR);
                hashMapDR.put(sectionDR, hashMapDR.get(sectionDR) + 1);
            }

            Section sectionDL = new Section(curr.dL, curr.areaDL);
            if(!hashMapDL.containsKey(sectionDL)){
                hashMapDL.put(sectionDL, 1);
            }else{
                numConflict = numConflict + hashMapDL.get(sectionDL);
                hashMapDL.put(sectionDL, hashMapDL.get(sectionDL) + 1);
            }
        }
        return numConflict;
    }
}
