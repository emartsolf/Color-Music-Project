import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import oscP5.*; 
import netP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class grids_o_colors extends PApplet {

// Your name here x
//String client_name = "Rob";
//int client_id = 9;
 




// MJD




MovieFrame frame;
Palette palette;







String client_name;
int client_id = -1; // starting value for 
String server_ip;
int server_port;
String local_client_ip; 
int local_client_port;

String preferences_file = "gridClient_preferences.txt";




OscP5 oscP5;
/* a NetAddress contains the ip address and port number of a remote location in the network. */
NetAddress myBroadcastLocation;
NetAddress myLocalMachine;

int rectX, rectY;      // Position of square button
int circleX, circleY;  // Position of circle button
int rectSize = 90;     // Diameter of rect
int circleSize = 93;   // Diameter of circle

int screenSizeX, screenSizeY;

int circleColor;
int baseColor = color(0);
int rectHighlight, circleHighlight;
int currentColor;
boolean rectOver = false;
boolean circleOver = false;

int defaultColor = 0xff808080;
int black = 0;
int red = 0xffff807f;
int blue = 0xff7fcaff;
int liteGreen = 0xff7aff7a;
int yellow = 0xfffeff79;
int orange = 0xffffa845;
int purple = 0xff695dff;
int liteBlue = 0xff7cffff;
int pink = 0xffff7bff;
int green = 0xff62AA5E;
int grey = 0xff808080;


int[] gSoundColors = { red, liteGreen, purple, yellow, orange, pink, blue, liteBlue, grey };    // Array to hold the colors for our sound palatte
int gSoundCellCount = gSoundColors.length;

int strokeHighlight = 255;//liteGreen;
int strokeDefault = 255;

int strokeWeightHighlight = 4;
int strokeWeightDefault = 1;
int strokeWeightSelected = 6;

int gActiveSound = -1;

int gGridWidth = 9;
int gGridHeight = 4;
int gCellCount = gGridWidth * gGridHeight;
int gGridOffsetX = 10;
int gGridOffsetY = 10;
int gCellOffsetX = 10;
int gCellOffsetY = 10;
int gCellWidth = 100;
int gCellHeight = 100;
int gGridPadLeft = 0;
int gGridPadRight = 0;
int gGridPadTop = 0;
int gGridPadBottom = 100;
int gSoundCellPadTop = 20;

int gLastSelectedCell = -2;

int gOscCurrentCount = 0;
int gLastNoteSent = -1;

boolean gEditorOpen = false;



int currentSoundCellOver = -1;
int currentCellOver = -1;

int currentSelectedSoundCell = -1;

	
public void settings() {
	screenSizeX = (gGridOffsetX * 2) + ((gCellWidth + gCellOffsetX) * gGridWidth) - gCellOffsetX + gGridPadLeft + gGridPadRight;
	screenSizeY = (gGridOffsetY * 2) + ((gCellHeight + gCellOffsetY) * (gGridHeight + 1) ) - gCellOffsetY + gGridPadTop + gGridPadBottom;

	screenSizeX = 550;
	screenSizeY = 600;
	size( screenSizeX, screenSizeY);
}

public void setup() {
	frame = new MovieFrame("inception.jpg", 0, 0, 477, 268);
	palette = new Palette(0, 270, 477, 300, 65, 3, 3); 
}

public void draw() {
	palette.update(frame.getAvgerageColor());
	background(currentColor);

	frame.display();
	palette.display();

	fill(frame.getAvgerageColor());
	rect(476, 1, 50, 267);
}

public void keyPressed() {	
}

public void setActiveSound(int sound)
{
}

public void mousePressed() {

	/*
	if (circleOver) {
		currentColor = circleColor;
	}
	if (rectOver) {
		currentColor = defaultColor;    
	}
*/

	if(mouseButton == LEFT) {
		frame.leftPress(mouseX, mouseY);
	} else if(mouseButton == RIGHT) {
		frame.rightPress(mouseX, mouseY);
	}
			
}

public void mouseReleased() {
	if(mouseButton == LEFT) {
		frame.releaseLeftMouse();
	} else if(mouseButton == RIGHT) {
		frame.releaseRightMouse();
	}
}

public void mouseDragged() 
{
	frame.update(mouseX, mouseY);
}

/*
boolean overRect(int x, int y, int width, int height) {
	if (mouseX >= x && mouseX <= x+width && 
		mouseY >= y && mouseY <= y+height) {
		return true;
	} else {
		return false;
	}
}

boolean overCircle(int x, int y, int diameter) {
	float disX = x - mouseX;
	float disY = y - mouseY;
	if (sqrt(sq(disX) + sq(disY)) < diameter/2 ) {
		return true;
	} else {
		return false;
	}
}
*/
class MovieFrame {
	int red = 0xffff807f;
	int x, y, width, height;
	PImage frame;

	int selX, selY, selW, selH;

	boolean leftSelected;
	int selOffX, selOffY;

	boolean rightSelected;
	int zone;
	int zoneStartX, zoneStartY;

	public MovieFrame(String frame, int _x, int _y, int _width, int _height) {
		x = _x;
		y = _y;
		width = _width;
		height = _height;

		changeFrame(frame);

		selX = 10;
		selY = 10;
		selW = 75;
		selH = 75;
		leftSelected = false;

		rightSelected = false;
		zone = 0;
	}

	private boolean overRect(int mouseX, int mouseY, int x, int y, int width, int height) {
		if (mouseX >= x && mouseX <= x+width && 
			mouseY >= y && mouseY <= y+height)
			return true;
		return false;
	}

	public void leftPress(int mouseX, int mouseY) {
		if(overRect(mouseX, mouseY, selX, selY, selW, selH)) {
			leftSelected = true;
			selOffX = mouseX - selX;
			selOffY = mouseY - selY;
		}
	}

	public void rightPress(int mouseX, int mouseY) {
		if(overRect(mouseX, mouseY, selX, selY, selW, selH)) {
			float xPer = (mouseX - selX) / ((float) selW);
			float yPer = (mouseY - selY) / ((float) selH);

			// Zone: 0 - top left, 1 - top right, 2 - bottom left, 3 - bottom right
			int zStart = (yPer <= .5f) ? 0 : 2;
			zone = ((xPer <= .5f) ? 0 : 1) + zStart;

			zoneStartX = mouseX;
			zoneStartY = mouseY;

			print("Zone", zone, "\n");

			rightSelected = true;
		}
	}

	public void releaseRightMouse()  {
		rightSelected = false;
	}

	public void releaseLeftMouse()  {
		leftSelected = false;
	}

	public void changeFrame(String newFrame) {
		frame = loadImage(newFrame);
	}

	public int getAvgerageColor() {
		float r = 0;
		float g = 0;
		float b = 0;

		int num = 0;
		for(int i = 0; i < selW; i++) {
			for(int j = 0; j < selH; j++) {
				int c = frame.get(selX + i, selY + j);
				r += red(c);
				g += green(c);
				b += blue(c);
				num++;
			}
		}

		return color(r/num, g/num, b/num);
	}

	public void update(int mouseX, int mouseY) {
		if(leftSelected) {
			selX = mouseX - selOffX;
			selY = mouseY - selOffY;
		}

		if(rightSelected) {
			int dX = mouseX - zoneStartX;
			int dY = mouseY - zoneStartY;
			if(zone == 3) {
				selW += dX;
				selH += dY;
			} else if(zone == 0) {
				selX += dX;
				selY += dY;

				selW -= dX;
				selH -= dY;

			} else if(zone == 1) {
				selW += dX;

				selY += dY;
				selH -= dY;
			} else if(zone == 2) {
				selH += dY;

				selX += dX;
				selW -= dX;
			}
			zoneStartX = mouseX;
			zoneStartY = mouseY;
		}
	}
	
	public void display() {
		image(frame, x, y, width, height);

		stroke(red);
		strokeWeight(5);
		noFill();
		rect(selX, selY, selW, selH);		 
	}
}
class Palette {
	int red = 0xffff807f;

	int currentColor;
	int padX, padY;
	int x, y, width, height, boxSize, boxHor, boxVer;
	public Palette(int _x, int _y, int _width, int _height, int _box_size, int _box_hor, int _box_ver) {
		x = _x;
		y = _y;
		width = _width;
		height = _height;
		boxSize = _box_size;
		boxHor = _box_hor;
		boxVer = _box_ver;

		padX = (width - boxSize * boxHor) / (boxHor - 1);
		padY = (height - boxSize * boxVer) / (boxVer - 1);

		currentColor = red;
		print(padX, padY, boxSize);
	}

	public void display() {
		stroke(red);
		strokeWeight(5);
		noFill();

		float shade = .95f;

		int temp = currentColor;
		for(int i = 0; i < boxVer; i++) {
			for(int j = 0; j < boxHor; j++) {
				fill(temp);
				rect(x + (padX + boxSize)*j, y + (padY + boxSize)*i, boxSize, boxSize);
				temp = color(red(temp) * shade, green(temp) * shade, blue(temp) * shade);
			}
		} 
	}

	public void update(int c) {
		currentColor = c;
	}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "grids_o_colors" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
