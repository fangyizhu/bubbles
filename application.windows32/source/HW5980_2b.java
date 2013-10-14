import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class HW5980_2b extends PApplet {

float PI = 3.1415926f;
ArrayList<PVector> v = new ArrayList<PVector>();
ArrayList<PVector> c = new ArrayList<PVector>(); //centers
ArrayList<Float> r = new ArrayList<Float>(); //radius
ArrayList<Boolean> kill = new ArrayList<Boolean>();
ArrayList<Boolean> killer = new ArrayList<Boolean>();
final int killEps2 = 30;
ArrayList<IntList> nbr = new ArrayList<IntList>();
ArrayList<FloatList> nd = new ArrayList<FloatList>();
PImage bg;
float camX;
float camY;
float camZ;
float eyeX;

public void setup() {
  bg = loadImage("bg.jpg");
  size(500, 500, P3D);
  v.add(new PVector(0.0f, -1.0f, 0.0f));
  v.add(new PVector(0.0f, -1.0f, 0.0f));
  c.add(new PVector(250.0f, 240.0f, 0.0f));
  c.add(new PVector(250.0f, 260.0f, 0.0f));
  r.add(20.0f);
  r.add(20.0f);
  kill.add(false);
  kill.add(false);
  killer.add(false);
  killer.add(false);
  camX = (float)width/2;
  camY = (float)height/2;
  camZ = 450.0f;
  eyeX = 0;
  background(0);
  smooth();
  frameRate(20);
}


public void draw() {
  background(bg);
  camera(camX, camY, camZ, width / 2, height / 2, eyeX, 0.0f, 1.0f, 0.0f);
  ambientLight(70, 5, 120);
  pointLight(51, 10, 226, width / 2, height / 2, 0);
  update();
  int len = c.size();
  stroke(100);
  fill(100);
  for (int i= 0; i < len; i++) {
    pushMatrix();
    translate(c.get(i).x, c.get(i).y, c.get(i).z);
    noStroke();
    sphere(r.get(i));
    popMatrix();
  }
}

public void findNeighbors() {
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
        if (i < j && !kill.get(i) && (l2-r.get(j)) < killEps2) {
          kill.set(j, true);
          killer.set(i, true);
        }
      }
    }
  }
}

public void mousePressed() {
  c.add(new PVector(mouseX, mouseY, 0.0f));
  r.add(random(10, 25));
  v.add(new PVector(0.0f, 0.0f, 0.0f));
  kill.add(false);
  killer.add(false);
}

public void update() {
  if (random(0, 2) < 0.05f) {
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
  for (int i = 0; i < len; i++) {
    PVector F = new PVector(0.0f, 0.0f, 0.0f);
    F.add(new PVector(0, g, 0));
    if (c.get(i).y <= r.get(i)) {
      c.get(i).y = r.get(i);
      v.get(i).y = 0;
      v.get(i).mult(0.9f);
    }

    if (c.get(i).x <= r.get(i)) {
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
      float cnb = (1.0f/nbi + 1.0f/nbj)/2;
      float cdist = (nd.get(i).get(n) - max(r.get(i), r.get(j)))/min(r.get(i), r.get(j));
      PVector temp2 = PVector.sub(c.get(i).get(), c.get(j).get());
      temp2.mult(ka*cnb*cdist/nd.get(i).get(n));
      F.add(temp2);
    }
    F.mult(1.0f/(kv + kair));

    float sv = v.get(i).mag();
    if (sv > maxv) {
      v.set(i, PVector.mult(v.get(i), maxv/sv));
    }
    v.set(i, PVector.add(v.get(i), new PVector(random(-1, 1), random(-1, 1), random(-0.5f, 0.5f))));
    c.set(i, PVector.add(c.get(i), PVector.mult(v.get(i), 0.05f)));
  }

  for (int i = (len -1); i >= 0; i--) {
    if (killer.get(i)) {
      r.set(i, 0.5f*r.get(i));
      killer.set(i, false);
    }
    if (kill.get(i)) {
      v.remove(i);
      c.remove(i);
      r.remove(i);
      kill.remove(i);
      println(i + "killed");
    }
  }
}

public void keyPressed() {
  switch (key) {
  case 'w':
    camZ -= 20;
    break;
  case 's':
    camZ += 20;
    break;
  case 'a':
    camX -= 20;
    eyeX -= 20;
    break;
  case 'd':
    camX += 20;
    eyeX += 20;
    break;
  }
  println(camX + " " + eyeX);
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "HW5980_2b" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
