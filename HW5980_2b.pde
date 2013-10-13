float PI = 3.1415926;
ArrayList<PVector> v = new ArrayList<PVector>();
ArrayList<PVector> c = new ArrayList<PVector>(); //centers
ArrayList<Float> r = new ArrayList<Float>(); //radius
ArrayList<Boolean> kill = new ArrayList<Boolean>();
ArrayList<Boolean> killer = new ArrayList<Boolean>();
final int killEps2 = 10;
ArrayList<IntList> nbr = new ArrayList<IntList>();
ArrayList<FloatList> nd = new ArrayList<FloatList>();

void setup() {
  v.add(new PVector(0.0, -1.0));
  v.add(new PVector(0.0, -1.0));
  c.add(new PVector(150.0, 70.0));
  c.add(new PVector(150.0, 100.0));
  r.add(20.0);
  r.add(20.0);
  kill.add(false);
  kill.add(false);

  size(500, 500);
  background(0);
  smooth();
  frameRate(30);
}


void draw() {
  background(0);
  update();
  int len = c.size();
  stroke(100);
  fill(100);
  for (int i= 0; i < len; i++) {
    ellipse(c.get(i).x, c.get(i).y, r.get(i)*2, r.get(i)*2);
    /*
    beginShape();
    for (int j = 0; j < 20; j++) {
      float angle = 2*(PI/20)*j;
      float x=c.get(i).x+sin(angle)*r.get(i);
      float y=c.get(i).y+cos(angle)*r.get(i);
      vertex(x, y);
    }
    endShape(CLOSE);
    */
  }
}

void findNeighbors() {
  nbr.clear();
  int len = c.size();
  for (int i = 0; i < len; i++) {
    nbr.add(new IntList());
    nd.add(new FloatList());
    for (int j = 0; j < len; j++) {
      if (i != j) {
        PVector d = c.get(i).get();
        d.sub(c.get(j));
        float l2 = PVector.dot(d, d);
        float r2 = r.get(i) + r.get(j);
        r2 *= r2;
        if (l2 < r2) {
          nbr.get(i).append(j);
          nd.get(i).append(sqrt(l2));
        }
        /*
        if (i < j && !kill.get(i) && (l2-r.get(j)) < killEps2) {
          kill.set(j, true);
          killer.set(i, true);
        }
        */
      }
    }
  }
}

void on_mouse_press() {
}
/*
def on_mouse_press(x, y, button, modifiers):
 print (x,y)
 c.append(np.array([float(x),float(y)]))
 r.append(20)#float(rnd.uniform(15,30)))
 v.append(np.array([0.,0.]))
 kill.append(False)
 killer.append(False)
 */

void update() {
  if (random(0, 2) < 0.05) {
    on_mouse_press();
    //on_mouse_press(rnd.gauss(200,100),rnd.uniform(300,400),False,False)
  }
  findNeighbors();
  float g = -20;
  float kr = 40;
  float ka = 50;  //~80 for sticky look, 1 for stiff
  float kair = 1;
  float maxv = 5;
  float kv = 1;

  int len = c.size();
  for(int i = 0; i < len; i++) {
    PVector F = new PVector(0.0, 0.0);
    F.add(new PVector(0, g));
    if(c.get(i).y <= r.get(i)) {
      c.get(i).y = r.get(i);
      v.get(i).y = 0;
      v.get(i).mult(0.9);
    }
    
    if(c.get(i).x <= r.get(i)) {
      c.get(i).x = r.get(i);
      v.get(i).x = 0;
    }
    int len2 = nbr.get(i).size();
    for (int n = 0; n < len2; n++) {
      int j = nbr.get(i).get(n);
      //F += kr*(1/(nd[i][n]) - 1/(r[i]+r[j]))*(c[i]-c[j])
      PVector temp = PVector.sub(c.get(i).get(), c.get(j).get());
      float scalar = kr*(1/(nd.get(i).get(n)) - 1/(r.get(i)+r.get(j)));
      temp.mult(scalar);
      F.add(temp);
      
      //F += ka*cnb*cdist*(c[j]-c[i])/nd[i][n]
      int nbi = nbr.get(i).size();
      int nbj = nbr.get(j).size();
      float cnb = (1.0/nbi + 1.0/nbj)/2;
      float cdist = (nd.get(i).get(n) - max(r.get(i), r.get(j)))/min(r.get(i), r.get(j));
      PVector temp2 = PVector.sub(c.get(i).get(), c.get(j).get());
      temp2.mult(ka*cnb*cdist/nd.get(i).get(n));
      F.add(temp2);
    }
    F.mult(1.0/(kv + kair));
    
    float sv = v.get(i).mag();
    if(sv > maxv) {
      v.set(i, PVector.mult(v.get(i), maxv/sv));
    }
    v.set(i, PVector.add(v.get(i), new PVector(random(-1, 1), random(-1,1))));
    c.set(i, PVector.add(c.get(i), PVector.mult(v.get(i), 0.05)));
  }
}

/*      
        v[i] = (1.0/(kv + kair))*(F)
        sv = np.linalg.norm(v) #size of v
        if sv > maxv: v[i] = maxv / sv * v[i]
        v[i] += .1*np.array([rnd.uniform(-1,1),rnd.uniform(-1,1)])
        c[i] += v[i]*dt
        #print "v",i,":",v[i]
        
    for i in reversed(list(range(len(c)))):
        if killer[i]:
            r[i] *= 1.05
            killer[i] = False
        if kill[i]:
            v.pop(i)
            c.pop(i)
            r.pop(i)
            del kill[i]
    */
