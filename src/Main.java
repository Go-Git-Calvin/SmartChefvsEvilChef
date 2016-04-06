import java.io.*;
import java.util.*;

/**
 * Created by Calvin on 4/5/16.
 *
 * Smart Chef vs Evil Chef Programming Practice
 *
 * */
public class Main {

    public static class Node<K> implements Comparable<Node<K>> {

        public static long ncount;
        public final K value;
        public final long priority;
        public final long id;

        Node(K value, long priority) {
            this.value = value;
            this.priority = priority;
            this.id = ncount++;
        }

        public int compareTo(Node<K> o) {
            int ret = Long.compare(priority, o.priority);
            if(ret == 0) ret = Long.compare(id, o.id);

            return ret;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;

            result = prime * result + (int) (id ^ (id >>> 32));
            result = prime * result + (int) (priority ^ (priority >>> 32));

            return result;
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;

            Node other = (Node) obj;

            if (id != other.id) return false;
            if (priority != other.priority) return false;

            return true;
        }
    }


    static class PQueue<K> {
        private Map<K,Node<K>> map;
        private SortedSet<Node<K>> set = new TreeSet<>();

        PQueue() {
            map = new HashMap<>();
        }

        Node<K> insert(K k, long p){
            Node<K> ret = new Node(k,p);
            map.put(k, ret);
            set.add(ret);

            return ret;
        }

        Node<K> changeKey(K k, long p) {
            Node n = map.get(k);

            if(n == null) {
                return null;
            }

            set.remove(n);
            Node<K> ret = new Node<K>(k,p);
            map.put(k, ret);
            set.add(ret);

            return ret;
        }

        private boolean isEmpty() {
            return set.isEmpty();
        }

        public K extractMin() {
            if(set.isEmpty()) {
                return null;
            }
            Node<K> n = set.first();
            set.remove(n);

            return n.value;
        }

        public boolean remove(K k) {
            Node n = map.get(k);

            if(n == null) {
                return false;
            }
            return set.remove(n);
        }

        public boolean contains(K k) {
            return map.containsKey(k);
        }

        public long getPriority(K k) {
            return map.get(k).priority;
        }

        public Iterable<K> keys(){
            return map.keySet();
        }
    }

    interface IAStar<K> {
        Iterable<K> neighbors(K k);
        long dist(K a, K b);
        long heuristic(K a, K b);
    }

    public static class AStar<K> {

        private final IAStar<K> data;
        private PQueue<K> open;
        private Map<K,Long> G;
        private Set<K> closed;
        private Map<K,K> pred;

        public AStar(IAStar<K> data) {
            this.data = data;
        }

        private long getG(K k) {
            Long g = G.get(k);
            if(g == null) {
                return Long.MAX_VALUE;
            }
            return g;
        }

        public List<K> go(K start, K end) {
            return go(start,end,0);
        }

        public List<K> go(K start, K end, long maxtime) {
            long t0 = 0;

            if(maxtime > 0) {
                t0 = System.currentTimeMillis();
            }
            //open = new PQueue<K>(new TreeMap<K>());
            G = new TreeMap<>();
            closed = new TreeSet<>();
            pred = new TreeMap<>();
            long count = 0;
            open.insert(start,data.heuristic(start, end));
            G.put(start,0L);

            while(!open.isEmpty()) {
                count++;

                if(maxtime>0 && (count %10 == 0)) {
                    long t1 = System.currentTimeMillis();
                    if(t1 > t0 + maxtime)
                        return new ArrayList<>();
                }
                K k = open.extractMin();
                long dk = G.get(k);

                if(k.equals(end)) {
                    break;
                }
                closed.add(k);

                for(K q : data.neighbors(k)) {
                    if(closed.contains(q)) {
                        continue;
                    }
                    long temp = dk + data.dist(k, q);

                    if(!open.contains(q)) {
                        open.insert(q,temp+data.heuristic(q, end));
                    }
                    else {
                        if(temp >= getG(q)) {
                            continue;
                        }
                        open.changeKey(q,temp+data.heuristic(q, end));
                    }
                    G.put(q, temp);
                    pred.put(q, k);
                }
            }
            List<K> ret = new ArrayList<>();
            K k = end;

            do {
                ret.add(k);
                k = pred.get(k);
            }
            while(k != null);
            return ret;
        }
    }

    public static class Point {
        final int x,y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Point move(int dir) {
            switch(dir) {
                case 0: return new Point(x-1,y);
                case 1: return new Point(x,y+1);
                case 2: return new Point(x+1,y);
                case 3: return new Point(x,y-1);
            }
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            Point other = (Point) obj;

            return x== other.x && y == other.y;
        }
    }

    public static class Maze {
        boolean[][] data;
        Point in,out;
        int h,w;
        int n;
        int[][] graph;
        int[] dout;
        int[] mv;
        Point[] points;

        public Maze(int height, int width) {
            h = height;
            w = width;
            data = new boolean[height][width];
        }

        public boolean get(Point a) {
            if(!inbounds(a)) {
                return false;
            }
            return data[a.x][a.y];
        }

        public void set(Point a, boolean b) {
            data[a.x][a.y] = b;
        }

        public void output() {
            int i = 0;
            System.out.println(h + " " + w);

            for(boolean[] a: data) {
                int j=0;

                for(boolean b: a) {
                    if(i== in.x && j == in.y) {
                        System.out.print("S");
                    }
                    else  if(i== out.x && j == out.y) {
                        System.out.print("E");
                    }
                    else {
                        System.out.print(b? "." : "#");
                    }
                    j++;
                }
                i++;
                System.out.println();
            }
        }


        public void buildGraph() {
            List<Point> pp = Arrays.asList(points);
            n = pp.size();

            graph = new int[n][4];
            HashMap<Point, Integer> map = new HashMap<>();

            for(int i=0; i<n; ++i) {
                map.put(pp.get(i), i);
            }
            for(int i=0; i<n; ++i) {
                Point p = pp.get(i);

                for(int j=0; j<4; ++j) {
                    Point q = p.move(j);
                    Integer r = map.get(q);
                    graph[i][j] = (r == null ? i : r);
                }
            }
        }

        public Point move(Point p, int k) {
            Point q = p.move(k);
            if(inbounds(q) && data[q.x][q.y]) {
                return q;
            }
            return p;
        }

        public void computeDistances() {
            dout = getDistances(1);
            int n = graph.length;
            mv = new int[n];

            for(int i=0; i<graph.length; ++i) {
                if(i==1) {
                    continue;
                }

                for(int j=0; j<4; ++j) {
                    if(dout[graph[i][j]] == dout[i]-1) {
                        mv[i] = j;
                        continue;
                    }
                }
            }
        }


        public int[] getDistances(int x) {
            PQueue<Integer> Q = new PQueue<>();
            int[] ret = new int[n];
            Q.insert(x, 0);
            ret[x] = 0;

            for(int i = 0; i < n; ++i) {

                if(i == x) {
                    continue;
                }
                Q.insert(i, Integer.MAX_VALUE);
                ret[i] = Integer.MAX_VALUE;
            }
            while(!Q.isEmpty()) {
                int y = Q.extractMin();
                for(int z : graph[y]) {
                    if(z == y) {
                        continue;
                    }
                    int d = ret[y]+1;
                    if(d < ret[z]) {
                        ret[z] = d;
                        Q.changeKey(z, d);
                    }
                }
            }
            return ret;
        }

        public void init() {
            getPoints();
            buildGraph();
            computeDistances();
            Arrays.fill(graph[1], 1);
        }

        private boolean inbounds(Point a) {
            if(a.x <0 || a.x>=h) {
                return false;
            }
            if(a.y <0 || a.y>=w) {
                return false;
            }
            return true;
        }


        public void  getPoints() {
            List<Point> ret = new ArrayList<>();
            ret.add(in);
            ret.add(out);

            for(int i=0; i<h; ++i) {

                for(int j = 0; j < w; ++j) {
                    if(i == in.x && j == in.y) {
                        continue;
                    }
                    if(i == out.x && j == out.y) {
                        continue;
                    }
                    if(data[i][j]) ret.add(new Point(i, j));
                }
            }
            points = ret.toArray(new Point[0]);
        }

        public boolean check(String s) {
            Point p = in;

            for(int i = 0; i < s.length(); ++i) {
                switch(s.charAt(i)) {
                    case 'N': p = move(p,0);break;
                    case 'E': p = move(p,1);break;
                    case 'S': p = move(p,2);break;
                    case 'W': p = move(p,3);break;
                }
                if(p.equals(out)) {
                    return true;
                }
            }
            return false;
        }
    }



    public static final String[] moves = new String[]{"N","E","S","W"};

    public static class MazeSolver {
        class State implements Comparable<State> {
            int[] pos;
            public State() {
                pos = new int[N];
            }
            public State(int[]p) {
                pos = p;
            }

            State move(int d) {
                State ret = new State();

                for(int i=0; i<pos.length; ++i) {
                    ret.pos[i] = mazes[i].graph[pos[i]][d];
                }
                return ret;
            }

            public void moveThis(int d) {
                for(int i=0; i<pos.length; ++i){
                    pos[i] = mazes[i].graph[pos[i]][d];
                }
            }

            public int distance(int i) {
                return mazes[i].dout[pos[i]];
            }

            public int[] distances() {
                int[] ret = new int[pos.length];

                for(int i = 0; i < ret.length; ++i) {
                    ret[i] = mazes[i].dout[pos[i]];
                }
                return ret;
            }

            @Override
            public int compareTo(State o) {
                for(int i=0; i<pos.length; ++i){
                    if(pos[i] > o.pos[i]) return 1;
                    if(pos[i] < o.pos[i]) return -1;
                }
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                State other = (State) obj;

                if (!Arrays.equals(pos, other.pos)) {
                    return false;
                }
                return true;
            }

            public State copy() {
                return new State(Arrays.copyOf(pos,pos.length));
            }
        }

        State in, out;
        Maze[] mazes;
        int N;

        public MazeSolver(Maze[] mazes) {
            this.mazes = mazes;
            N = mazes.length;
            in = new State();
            out = new State();
            Arrays.fill(out.pos,1);
        }

        public String solveSimple(State t) {
            State s = t.copy();
            String ret = "";

            while(!s.equals(out)) {
                int k = 0;
                int min = Integer.MAX_VALUE;

                for(int i = 0; i < N; ++i) {
                    int z = mazes[i].dout[s.pos[i]];
                    if(z == 0) continue;
                    if(z <min) {
                        min = z;
                        k = i;
                    }
                }
                while(s.pos[k]!=1) {
                    int mv = mazes[k].mv[s.pos[k]];
                    s.moveThis(mv);
                    ret += moves[mv];
                }
            }
            return ret;
        }

        public String solve(long t) {
            long t0 = System.currentTimeMillis();
            String r0 = solveSimple(in);

            for(double d : new double[]{1,1.5}) {
                long left = t-(System.currentTimeMillis()-t0);
                String r1 = solveAStar(d, left);

                if(r1.length()>0 && r1.length()<r0.length()) {
                    r0 = r1;
                }
            }
            return r0;
        }

        public String solveAStar(double d, long t) {

            IAStar<State> gdata = new IAStar<State>() {

                @Override
                public Iterable<State> neighbors(State k) {
                    return Arrays.asList(new State[] {
                            k.move(0),k.move(1),k.move(2),k.move(3)
                    });
                }

                @Override
                public long dist(State a, State b) {
                    return 1;
                }

                @Override
                public long heuristic(State a, State b) {
                    double ret = 0;
                    for(int i = 0; i < N; ++i) {
                        ret += Math.pow(a.distance(i),d);
                    }
                    ret = Math.pow(ret, 1/d);

                    return (long)(ret*d);
                }
            };

            AStar<State> as = new AStar<>(gdata);
            List<State> sol = as.go(in, out,t);
            String ret = "";

            for(int i = sol.size()-2; i >= 0; --i) {
                ret += moves[getMove(sol.get(i+1), sol.get(i))];
            }
            return ret;
        }

        public int getMove(State a, State b) {
            outer:

            for(int d=0; d<4; ++d) {
                for(int i = 0; i < N; ++i) {
                    if(mazes[i].graph[a.pos[i]][d] != b.pos[i]) {
                        continue outer;
                    }
                }
                return d;
            }
            return -1;
        }
    }


    public static Maze readMaze(BufferedReader br) throws IOException {
        String[] ss = br.readLine().split(" ");
        Maze ret = new Maze(Integer.parseInt(ss[0]),Integer.parseInt(ss[1]));

        for(int i = 0; i < ret.h; ++i) {
            String s = br.readLine();

            for(int j = 0; j < ret.w; ++j) {
                char c = s.charAt(j);

                if(c == '#') {
                    continue;
                }
                ret.data[i][j] = true;
                if(c == 'S') {
                    ret.in = new Point(i, j);
                }
                else if(c == 'E') {
                    ret.out = new Point(i, j);
                }
            }
        }
        ret.init();
        return ret;
    }

    public static MazeSolver readProblem(BufferedReader br) throws NumberFormatException, IOException {
        int n = Integer.parseInt(br.readLine());
        Maze[] mazes = new Maze[n];

        for(int i = 0; i < n; ++i) {
            mazes[i] = readMaze(br);
        }
        return new MazeSolver(mazes);
    }

    public static void goFile(String s) throws NumberFormatException, FileNotFoundException, IOException {
        go(new FileReader(new File(s)));
    }

    public static void go() throws NumberFormatException, IOException {
        go(new InputStreamReader(System.in));
    }

    public static void go(Reader r) throws NumberFormatException, IOException{
        BufferedReader br = new BufferedReader(r);
        int k = Integer.parseInt(br.readLine());

        for(int i = 0; i < k; ++i) {
            MazeSolver m = readProblem(br);
            System.out.println(m.solve(1500));
        }
    }

    public static void main(String[] args) throws Exception {
        go();
    }
} // end of class