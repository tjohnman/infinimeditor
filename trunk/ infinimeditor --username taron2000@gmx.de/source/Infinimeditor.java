import processing.core.*; 
import processing.xml.*; 

import java.awt.event.*; 

import java.applet.*; 
import java.awt.*; 
import java.awt.image.*; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class Infinimeditor extends PApplet {

/*
    Infinimeditor, a level editor for Zachtronics' Infiniminer
    Copyright (C) 2009 Joan Dolz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
*/

 // For the mouse wheel to work
 // For Infiniminer 1.5


	

String VERSION = "Infinimeditor v0.4";

//Define editor state variables
int DEBUG = 0;
int [][][][] level;
int [][][][] undo;
int [][] undocontrol;
int loaded;
int blockSize = 11;
int miniBlockSize = 3;
int command;
int [] coord;
boolean borderline;
String filename = "", mapName = "";
char view = 'Z';
int layer = 1;
int brush = 0; //changed to int
int brushtype;
int brushSize;
int team = 0;
Button[] buttons = new Button [20];
int buttonIndex = 0;
int mapBorderColor;
boolean scrolled; //mouseWheel fix hopefully
boolean changing;
int undostep;
int maxundo;

int[] brushColor = new int[22];

// Setup the environment
public void setup() {
  frame.addMouseWheelListener(new MouseWheelInput());
  textMode(SCREEN);
  textFont(loadFont("SansSerif-12.vlw"), 12);
  fill(0);
  command = 0;
  loaded = 0;
  size(1024, 768, P2D);
  background(255);
  noStroke();
  level = new int[64][64][64][2];
  undo = new int[64][64][2][11];
  undocontrol = new int[11][2];
  coord = new int [2];
  borderline = false;
  coord[0]=0;
  coord[1]=0;
  brushtype=1; //not changeable yet, but introduced 
  brushSize=1;
  scrolled=false;
  changing=false;
  undostep=0;
  maxundo=0;
  setupUI();
}
// Executed on every frame
public void draw() {
  background(255);
  fill(200);
  stroke(mapBorderColor);
  rect(blockSize*2-1, blockSize*2-1, 64*blockSize+1, 64*blockSize+1);
  rect(miniBlockSize*2 +1024-251,blockSize*2-1, 64*miniBlockSize+1, 64*miniBlockSize+1);
  rect(miniBlockSize*2 +1024-251, blockSize*4 + 64*miniBlockSize-1, 64*miniBlockSize+1, 64*miniBlockSize+1);
  noStroke();
  fill(0);
  if(loaded == 1) {
    drawView(view);
    
    //layerwarning
    if (layer == 0 || layer == 63){
    	fill(255,0,0);
    	text("Warning: NOT PART OF LEVEL!", 320, 320);
    	fill(0);
    }
    //show brushtype
    if (brushtype == 1) {
    	text("Brushtype: Square  Brushsize: "+str(brushSize), 1024-270, blockSize*5 +650);
    }
    //Space for other brushtypes
    
    if(mouseOnScreen()) {
    // If debug mode is ON, show block codes
      if(DEBUG == 1) {
         fill(255);
         rect(mouseX, mouseY, 50, 20);
         fill(0);
        if(view == 'X') {
          text(str(level[layer][floor((mouseY-blockSize*2)/blockSize)][floor((mouseX-blockSize*2)/blockSize)][0]), mouseX + 25, mouseY + 15);
        
        }
        if(view == 'Z') {
          text(str(level[floor((mouseX-blockSize*2)/blockSize)][floor((mouseY-blockSize*2)/blockSize)][layer][0]), mouseX + 25, mouseY + 15);
          
        }
        if(view == 'Y') {
          
        }
      }
      // Transform MouseCoords to GridCoords
      coord[0]=mouseX/blockSize -2;
      coord[1]=mouseY/blockSize -2;
      // Show coordinates
      if(view == 'X')  	{  
    	  text("Coordinates: X:"+str(layer+1)+" Y:"+str(coord[1]+1)+" Z:"+str(coord[0]+1), 1024-270, blockSize*6 +660);  
      }
      if(view == 'Y')  	{  
    	  text("Coordinates: X:"+str(coord[0]+1)+" Y:"+str(layer+1)+" Z:"+str(coord[1]+1), 1024-270, blockSize*6 +660);  
      }
      if(view == 'Z')  	{  
    	  text("Coordinates: X:"+str(coord[0]+1)+" Y:"+str(coord[1]+1)+" Z:"+str(layer+1), 1024-270, blockSize*6 +660);  
      }
          
      noFill();
      stroke(255);
      // Display a rect under the cursor to highlight the block that is going to be affected
      drawCursor();    
      //Cursor on minimap      
      rect(miniBlockSize*2 +1024-251 +(layer+1)*miniBlockSize, blockSize*2+(coord[0])*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
      rect(miniBlockSize*2 +1024-251 +(layer+1)*miniBlockSize, blockSize*4+(coord[1])*miniBlockSize+64*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
      noStroke();
      fill(0);
    }
  //Toggle Minimaps
    if(view == 'Z'){
    	text("Y-axis view",1024-240, blockSize*2 -2);
    	drawMiniView1('Y',coord[1]);
    	text("X-axis view",1024-240, blockSize*4 -2+64*miniBlockSize);
    	drawMiniView2('X',coord[0]);
    	//Cursor on minimap  
    	noFill();
        stroke(255);        
        rect(miniBlockSize*2 +1024-251 +(coord[0])*miniBlockSize, blockSize*2+layer*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
        rect(miniBlockSize*2 +1024-251 +(layer)*miniBlockSize, blockSize*4+(coord[1])*miniBlockSize+64*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
        noStroke();
        fill(0);
 	  }
    if(view == 'X'){
        text("Y-axis view",1024-240, blockSize*2 -2);
    	drawMiniView1('Y',coord[1]);
    	text("Z-axis view",1024-240, blockSize*4 -2+64*miniBlockSize);
    	drawMiniView2('Z',coord[0]);
    	//Cursor on minimap  
    	noFill();
        stroke(255);        
        rect(miniBlockSize*2 +1024-251 +layer*miniBlockSize, blockSize*2+(coord[0])*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
        rect(miniBlockSize*2 +1024-251 +(layer)*miniBlockSize, blockSize*4+(coord[1])*miniBlockSize+64*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
        noStroke();
        fill(0);
   	  }
    if(view == 'Y'){
        text("Z-axis view",1024-240, blockSize*2 -2);
    	drawMiniView1('Z',coord[1]);
    	text("X-axis view",1024-240, blockSize*4 -2+64*miniBlockSize);
    	drawMiniView2('X',coord[0]);
    	//Cursor on minimap  
    	noFill();
        stroke(255);        
        rect(miniBlockSize*2 +1024-251 +(coord[0])*miniBlockSize, blockSize*2+layer*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
        rect(miniBlockSize*2 +1024-251 +(coord[1])*miniBlockSize, blockSize*4+layer*miniBlockSize+64*miniBlockSize -1, miniBlockSize+1, miniBlockSize+1);
        noStroke();
        fill(0);
   	  }
    
  
   
  }
  // Show current loaded map and current slice
  // If a command is being executed, show a rectangle under the prompt (if any).
  if(command != 0) {
    fill(124, 255, 255);
    rect(0, 733, 600, 22);
  } else {
     if(loaded == 1) {
      text("\""+mapName+"\" loaded", blockSize*2, 748);
      text("Slice "+ str(layer+1) +" on axis "+view, 22, blockSize*2 -2);
    } else {
      text("No map loaded", blockSize*2, 748);
    }
  }
  // Manage loading and saving states
  fill(0);
  if(loaded == 2) {
    mapName = filename;
    loadLevel(mapName);
  }
  if(loaded == 3) {
    saveData();
  }
  // Draw the brush list and team selection
  for(int i=0; i<22; i++) {
    fill(brushColor[i]);
    if (i<13){
    rect(1024-270, blockSize*5 + i*15 +401, 10, 10);
    }else{
    rect(1024-145, blockSize*5 + (i-13)*15 +401, 10, 10);
    }
  }
  
  fill(0);
  //first row
  text("Empty", 1024-250, blockSize*6+ 400);
  text("Dirt", 1024-250, blockSize*6 + 415);
  text("Ore", 1024-250, blockSize*6 + 430);
  text("Gold", 1024-250, blockSize*6 + 445);
  text("Diamond", 1024-250, blockSize*6 + 460);
  text("Stone", 1024-250, blockSize*6 + 475);
  text("Ladder", 1024-250, blockSize*6 + 490);
  text("Explosives", 1024-250, blockSize*6 + 505);
  text("Jump block", 1024-250, blockSize*6 + 520);
  text("Shock block", 1024-250, blockSize*6 + 535);
  text("Red bank", 1024-250, blockSize*6 + 550);
  text("Blue bank", 1024-250, blockSize*6 + 565);
  text("Red beacon", 1024-250, blockSize*6 + 580);
  //second row
  text("Blue beacon", 1024-125, blockSize*6 + 400);
  text("Road block", 1024-125, blockSize*6 + 415);  
  text("Red solid", 1024-125, blockSize*6 + 430);
  text("Blue solid", 1024-125, blockSize*6 + 445);
  text("Steel", 1024-125, blockSize*6 + 460);
  text("\'Dig here!\'", 1024-125, blockSize*6 + 475);
  text("Lava", 1024-125, blockSize*6 + 490);
  text("Red force field", 1024-125, blockSize*6 + 505);
  text("Blue force field", 1024-125, blockSize*6 + 520);
  
  text("Spawning for:", 1024-270, blockSize*6 + 600);
  
  if(team == 0) {  // Neutral
    text("Neutral team", 1024-270, blockSize*6 + 615);
  }
  
  if(team == 1) {  // Red
    text("Red team", 1024-270, blockSize*6 + 615);
  }
  
  if(team == 2) {  // Blue
    text("Blue team", 1024-270, blockSize*6 + 615);
  }
  // Highlight currently selected brush
  stroke(127);
  noFill();
  if (brush < 13){
	  rect(1024-280, blockSize*5 + brush*15 - 2 +400, 125, 15);
  } else {
	  rect(1024-155, blockSize*5 + (brush-13)*15 - 2 +400, 125, 15);  
  }
  noStroke();
  displayGroundLevelIndicator();
  
  manageCommands();
  
  // Display program's version
  fill(0);
  text(VERSION, 1024-150, 768-blockSize*2);
}

//void setupUI()
//Arguments:  none
//Returns:    nothing
//Effect:     Sets the colors for the map border and the brush list. It also populates the menu.

public void setupUI() {
	  mapBorderColor = color(0, 0, 0);
	  
	  brushColor[0] = color(0, 0, 0);
	  brushColor[1] = color(125, 120, 0);
	  brushColor[2] = color(125, 120, 200);
	  brushColor[3] = color(200, 160, 0);
	  brushColor[4] = color(200, 200, 210);
	  brushColor[5] = color(127, 127, 127);
	  brushColor[6] = color(255, 0, 0);  // ladder
	  brushColor[7] = color(0, 0, 255);
	  brushColor[8] = color(0, 0, 0);  // jump
	  brushColor[9] = color(0, 0, 0);  // shock
	  brushColor[10] = color(0, 0, 0);  //red bank
	  brushColor[11] = color(0, 0, 0);  // blue bank
	  brushColor[12] = color(0, 0, 0);  // red beacon
	  brushColor[13] = color(0, 0, 0);  // blue beacon
	  brushColor[14] = color(32, 32, 32);  // road block
	  brushColor[15] = color(0, 0, 0);  // red block
	  brushColor[16] = color(0, 0, 0);  // blue block
	  brushColor[17] = color(64, 64, 64);
	  brushColor[18] = color(200, 200, 0);
	  brushColor[19] = color(200, 125, 0);
	  brushColor[20] = color(0, 0, 0);  // red ff
	  brushColor[21] = color(0, 0, 0);  //blue ff
	  
	  buttons[buttonIndex] = new Button("Create blank map", blockSize*2 + blockSize*27 - blockSize/2, 120, 4, 0);
	  buttonIndex++;
	  
	  buttons[buttonIndex] = new Button("Create flat map", blockSize*2 + blockSize*27 - blockSize/2, 160, 6, 0);
	  buttonIndex++;
	  
	  buttons[buttonIndex] = new Button("Save copy...", blockSize*2 + blockSize*27 - blockSize/2, 200, 7, 1);
	  buttonIndex++;
	  
	  buttons[buttonIndex] = new Button("Convert 1.3 Map to 1.5", blockSize*2 + blockSize*27 - blockSize/2, 240, 9,1);
	  buttonIndex++;
	}

//
//VIEW RENDERING
//

//void drawView()
//Arguments:  char axis
//Returns:    nothing
//Effect:     Run through the level array and render each block accordingly. Axis can be 'X', 'Y' or 'Z'.

public void drawView(char axis) {
  String info = "";
  if(axis=='Z') {
    for(int i=0; i<64; i++) {
      for(int j=0; j<64; j++) {
        info = "";
        switch(level[i][j][layer][0]) {
          case 0:
            break;
          case 1:
            fill(125, 120, 0);
            break;
          case 2:
            fill(125, 120, 200);
            break;
          case 3:
            fill(200, 160, 0);
            break;
          case 4:
            fill(200, 200, 210);
            break;
          case 5:
            fill(127, 127, 127);
            break;
          case 7:
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            info = "e";
            break;
          case 6:
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            info = "l";
            break;
          case 8:
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            info = "j";
            break;
          case 9:
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            info = "s";
            break;
          case 10:
          case 11:
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            info = "b";
            break;
          case 12:
          case 13:
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            info = "be";
            break;
          case 14:
              
                fill(32, 32, 32);             
              break;
          case 15:
          case 16:
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          case 17:
            fill(64, 64, 64);
            break;
          case 18:
            fill(200, 200, 0);
            info = "d";
            break;
          case 19:
            fill(200, 125, 0);
            break;
          case 20:
            info = "rf";
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          case 21:
            info = "bf";
            if(level[i][j][layer][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][j][layer][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][j][layer][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          default:
            fill(255, 255, 255);
            info = "X";
        }
        //println(level[i][layer][j][0]);
        rect(blockSize*2 + i*blockSize, blockSize*2 + j*blockSize, blockSize, blockSize);
        fill(0);
        if(DEBUG == 1) {
          text(str(level[i][j][layer][0]), blockSize*2 + i*blockSize - blockSize/2, blockSize*2 + j*blockSize + 10);
        } else {
          text(info, blockSize*2 + i*blockSize + 1, blockSize*2 + j*blockSize + 10);
        }
      }
    }
  }
  
  /**************************************/
  
  if(axis=='Y') {
    for(int i=0; i<64; i++) {
      for(int j=0; j<64; j++) {
        info = "";
        switch(level[i][layer][j][0]) {
          case 0:
            break;
          case 1:
            fill(125, 120, 0);
            break;
          case 2:
            fill(125, 120, 200);
            break;
          case 3:
            fill(200, 160, 0);
            break;
          case 4:
            fill(200, 200, 210);
            break;
          case 5:
            fill(127, 127, 127);
            break;
          case 7:
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            info = "e";
            break;
          case 8:
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            info = "j";
            break;
          case 6:
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            info = "l";
            break;
          case 9:
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            info = "s";
            break;
          case 10:
          case 11:
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            info = "b";
            break;
          case 12:
          case 13:
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            info = "be";
            break;
          case 14:             
                fill(32, 32, 32);             
              break;
          case 15:
          case 16:
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          case 17:
            fill(64, 64, 64);
            break;
          case 18:
            fill(200, 200, 0);
            info = "d";
            break;
          case 19:
            fill(200, 125, 0);
            break;
          case 20:
            info = "rf";
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          case 21:
            info = "bf";
            if(level[i][layer][j][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[i][layer][j][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[i][layer][j][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          default:
            fill(255, 255, 255);
            info = "X";
        }
        //println(level[i][layer][j][0]);
        rect(blockSize*2 + i*blockSize, blockSize*2 + j*blockSize, blockSize, blockSize);
        fill(0);
        if(DEBUG == 1) {
          text(str(level[i][layer][j][0]), blockSize*2 + i*blockSize - blockSize/2, blockSize*2 + j*blockSize + 10);
        } else {
          text(info, blockSize*2 + i*blockSize + 1, blockSize*2 + j*blockSize + 10);
        }
      }
    }
  }
  
  /******************************************/
  
  if(axis=='X') {
    for(int i=0; i<64; i++) {
      for(int j=0; j<64; j++) {
        info = "";
        switch(level[layer][j][i][0]) {
          case 0:
            break;
          case 1:
            fill(125, 120, 0);
            break;
          case 2:
            fill(125, 120, 200);
            break;
          case 3:
            fill(200, 160, 0);
            break;
          case 4:
            fill(200, 200, 210);
            break;
          case 5:
            fill(127, 127, 127);
            break;
          case 7:
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            info = "e";
            break;
          case 6:
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            info = "l";
            break;
          case 8:
            info = "j";
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          case 9:
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            info = "s";
            break;
          case 10:
          case 11:
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            info = "b";
            break;
          case 12:              
          case 13:
              if(level[layer][j][i][1] == 1) {
                fill(255, 0, 0);
              }
              if(level[layer][j][i][1] == 2) {
                fill(0, 0, 255);
              }
              if(level[layer][j][i][1] == 0) {
                fill(255, 0, 255);
              }
              info = "be";
              break;
          case 14:              
                fill(32, 32, 32);             
              break;
          case 15:
          case 16:
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          case 17:
            fill(64, 64, 64);
            break;
          case 18:
            fill(200, 200, 0);
            info = "d";
            break;
          case 19:
            fill(200, 125, 0);
            break;
          case 20:
            info = "rf";
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          case 21:
            info = "bf";
            if(level[layer][j][i][1] == 1) {
              fill(255, 0, 0);
            }
            if(level[layer][j][i][1] == 2) {
              fill(0, 0, 255);
            }
            if(level[layer][j][i][1] == 0) {
              fill(255, 0, 255);
            }
            break;
          default:
            fill(255, 255, 255);
            info = "X";
        }
        //println(level[i][j][layer][0]);
        rect(blockSize*2 + i*blockSize, blockSize*2 + j*blockSize, blockSize, blockSize);
        fill(0);
        if(DEBUG == 1) {
          text(str(level[layer][j][i][0]), blockSize*2 + i*blockSize - blockSize/2, blockSize*2 + j*blockSize + 10);
        } else {
          text(info, blockSize*2 + i*blockSize + 1, blockSize*2 + j*blockSize + 10);
        }
      }
    }
  }
}

public void drawMiniView1(char axis, int crd) {
	  
	  if(axis=='Z') {
	    for(int i=0; i<64; i++) {
	      for(int j=0; j<64; j++) {
	        
	        switch(level[i][j][crd][0]) {
	          case 0:
	            break;
	          case 1:
	            fill(125, 120, 0);
	            break;
	          case 2:
	            fill(125, 120, 200);
	            break;
	          case 3:
	            fill(200, 160, 0);
	            break;
	          case 4:
	            fill(200, 200, 210);
	            break;
	          case 5:
	            fill(127, 127, 127);
	            break;
	          case 7:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 6:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 8:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 9:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	          
	            break;
	          case 10:
	          case 11:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 12:
	          case 13:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 14:
	              
	                fill(32, 32, 32);             
	              break;
	          case 15:
	          case 16:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 17:
	            fill(64, 64, 64);
	            break;
	          case 18:
	            fill(200, 200, 0);
	            
	            break;
	          case 19:
	            fill(200, 125, 0);
	            break;
	          case 20:
	            
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 21:
	            
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          default:
	            fill(255, 255, 255);
	            
	        }
	        
	        rect(miniBlockSize*2 + i*miniBlockSize+1024-250, blockSize*2 + j*miniBlockSize, miniBlockSize, miniBlockSize);
	        fill(0);
	        
	      }
	    }
	  }
	  
	  /**************************************/
	  
	  if(axis=='Y') {
	    for(int i=0; i<64; i++) {
	      for(int j=0; j<64; j++) {
	        
	        switch(level[i][crd][j][0]) {
	          case 0:
	            break;
	          case 1:
	            fill(125, 120, 0);
	            break;
	          case 2:
	            fill(125, 120, 200);
	            break;
	          case 3:
	            fill(200, 160, 0);
	            break;
	          case 4:
	            fill(200, 200, 210);
	            break;
	          case 5:
	            fill(127, 127, 127);
	            break;
	          case 7:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 8:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 6:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 9:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 10:
	          case 11:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 12:
	          case 13:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 14:             
	                fill(32, 32, 32);             
	              break;
	          case 15:
	          case 16:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 17:
	            fill(64, 64, 64);
	            break;
	          case 18:
	            fill(200, 200, 0);
	            
	            break;
	          case 19:
	            fill(200, 125, 0);
	            break;
	          case 20:
	           
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 21:
	            
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          default:
	            fill(255, 255, 255);
	            
	        }
	        
	        rect(miniBlockSize*2 + i*miniBlockSize+1024-250, blockSize*2 + j*miniBlockSize, miniBlockSize, miniBlockSize);
	        fill(0);
	        
	      }
	    }
	  }
	  
	  /******************************************/
	  
	  if(axis=='X') {
	    for(int i=0; i<64; i++) {
	      for(int j=0; j<64; j++) {
	       
	        switch(level[crd][j][i][0]) {
	          case 0:
	            break;
	          case 1:
	            fill(125, 120, 0);
	            break;
	          case 2:
	            fill(125, 120, 200);
	            break;
	          case 3:
	            fill(200, 160, 0);
	            break;
	          case 4:
	            fill(200, 200, 210);
	            break;
	          case 5:
	            fill(127, 127, 127);
	            break;
	          case 7:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 6:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 8:
	            
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 9:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 10:
	          case 11:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	         
	            break;
	          case 12:              
	          case 13:
	              if(level[crd][j][i][1] == 1) {
	                fill(255, 0, 0);
	              }
	              if(level[crd][j][i][1] == 2) {
	                fill(0, 0, 255);
	              }
	              if(level[crd][j][i][1] == 0) {
	                fill(255, 0, 255);
	              }
	              
	              break;
	          case 14:              
	                fill(32, 32, 32);             
	              break;
	          case 15:
	          case 16:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 17:
	            fill(64, 64, 64);
	            break;
	          case 18:
	            fill(200, 200, 0);
	         
	            break;
	          case 19:
	            fill(200, 125, 0);
	            break;
	          case 20:
	           
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 21:
	     
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          default:
	            fill(255, 255, 255);
	            
	        }
	        
	        rect(miniBlockSize*2 + i*miniBlockSize +1024-250, blockSize*2 + j*miniBlockSize, miniBlockSize, miniBlockSize);
	        fill(0);	      
	      }
	    }
	  }
	}

public void drawMiniView2(char axis, int crd) {
	  
	  if(axis=='Z') {
	    for(int i=0; i<64; i++) {
	      for(int j=0; j<64; j++) {
	        
	        switch(level[i][j][crd][0]) {
	          case 0:
	            break;
	          case 1:
	            fill(125, 120, 0);
	            break;
	          case 2:
	            fill(125, 120, 200);
	            break;
	          case 3:
	            fill(200, 160, 0);
	            break;
	          case 4:
	            fill(200, 200, 210);
	            break;
	          case 5:
	            fill(127, 127, 127);
	            break;
	          case 7:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 6:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 8:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 9:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	          
	            break;
	          case 10:
	          case 11:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 12:
	          case 13:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 14:
	              
	                fill(32, 32, 32);             
	              break;
	          case 15:
	          case 16:
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 17:
	            fill(64, 64, 64);
	            break;
	          case 18:
	            fill(200, 200, 0);
	            
	            break;
	          case 19:
	            fill(200, 125, 0);
	            break;
	          case 20:
	            
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 21:
	            
	            if(level[i][j][crd][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][j][crd][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][j][crd][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          default:
	            fill(255, 255, 255);
	            
	        }
	        
	        rect(miniBlockSize*2 + i*miniBlockSize+1024-250, blockSize*4 + j*miniBlockSize+ 64*miniBlockSize, miniBlockSize, miniBlockSize);
	        fill(0);
	        
	      }
	    }
	  }
	  
	  /**************************************/
	  
	  if(axis=='Y') {
	    for(int i=0; i<64; i++) {
	      for(int j=0; j<64; j++) {
	        
	        switch(level[i][crd][j][0]) {
	          case 0:
	            break;
	          case 1:
	            fill(125, 120, 0);
	            break;
	          case 2:
	            fill(125, 120, 200);
	            break;
	          case 3:
	            fill(200, 160, 0);
	            break;
	          case 4:
	            fill(200, 200, 210);
	            break;
	          case 5:
	            fill(127, 127, 127);
	            break;
	          case 7:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 8:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 6:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 9:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 10:
	          case 11:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 12:
	          case 13:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 14:             
	                fill(32, 32, 32);             
	              break;
	          case 15:
	          case 16:
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 17:
	            fill(64, 64, 64);
	            break;
	          case 18:
	            fill(200, 200, 0);
	            
	            break;
	          case 19:
	            fill(200, 125, 0);
	            break;
	          case 20:
	           
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 21:
	            
	            if(level[i][crd][j][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[i][crd][j][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[i][crd][j][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          default:
	            fill(255, 255, 255);
	            
	        }
	        
	        rect(miniBlockSize*2 + i*miniBlockSize+1024-250, blockSize*4 + j*miniBlockSize+ 64*miniBlockSize, miniBlockSize, miniBlockSize);
	        fill(0);
	        
	      }
	    }
	  }
	  
	  /******************************************/
	  
	  if(axis=='X') {
	    for(int i=0; i<64; i++) {
	      for(int j=0; j<64; j++) {
	       
	        switch(level[crd][j][i][0]) {
	          case 0:
	            break;
	          case 1:
	            fill(125, 120, 0);
	            break;
	          case 2:
	            fill(125, 120, 200);
	            break;
	          case 3:
	            fill(200, 160, 0);
	            break;
	          case 4:
	            fill(200, 200, 210);
	            break;
	          case 5:
	            fill(127, 127, 127);
	            break;
	          case 7:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 6:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            
	            break;
	          case 8:
	            
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 9:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	           
	            break;
	          case 10:
	          case 11:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	         
	            break;
	          case 12:              
	          case 13:
	              if(level[crd][j][i][1] == 1) {
	                fill(255, 0, 0);
	              }
	              if(level[crd][j][i][1] == 2) {
	                fill(0, 0, 255);
	              }
	              if(level[crd][j][i][1] == 0) {
	                fill(255, 0, 255);
	              }
	              
	              break;
	          case 14:              
	                fill(32, 32, 32);             
	              break;
	          case 15:
	          case 16:
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 17:
	            fill(64, 64, 64);
	            break;
	          case 18:
	            fill(200, 200, 0);
	         
	            break;
	          case 19:
	            fill(200, 125, 0);
	            break;
	          case 20:
	           
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          case 21:
	     
	            if(level[crd][j][i][1] == 1) {
	              fill(255, 0, 0);
	            }
	            if(level[crd][j][i][1] == 2) {
	              fill(0, 0, 255);
	            }
	            if(level[crd][j][i][1] == 0) {
	              fill(255, 0, 255);
	            }
	            break;
	          default:
	            fill(255, 255, 255);
	            
	        }
	        
	        rect(miniBlockSize*2 + i*miniBlockSize +1024-250, blockSize*4 + j*miniBlockSize+ 64*miniBlockSize, miniBlockSize, miniBlockSize);
	        fill(0);	      
	      }
	    }
	  }
	}

//
//MOUSE AND KEYBOARD EVENTS
//

//void keyReleased()
//Callback

public void keyReleased(){
	if(key == 'f') {
		changing=false;
	}
}

//void keyPressed()
//Callback

public void keyPressed() {
  if(key == 'm') {
    command = abs(command - 5);
  }
  if(key == 'b') {
      if (borderline == true){
      	borderline=false;
      }else{
      	borderline=true;
      }
    }
 if(command == 0) {
    if(key == 'l') {
      command = 1;
      filename = "";
    }
    if(key == 's') {
      command = 3;
    }
    if(key == '+' || key == '=') {
      if(layer < 63) {
        layer++;
      }
    }
    if(key == '-' || key == '_') {
      if(layer > 0) {
        layer--;
      }
    }
    if(key == 'x') {
      view = 'X';
    }
    if(key == 'y') {
      view = 'Y';
    }
    if(key == 'z') {
      view = 'Z';
    }
    if(key == 'u') { //undo
    	if (undostep >= 0){
    		undostep--;
            if (undostep < 0){
            	undostep=0;
            }
            
    		restoreUndo();       
            
    	}
      }
    if(key == 'i') {//redo
    	if (undostep >= 0){
    		
            if (undostep < maxundo-1){
            	undostep++;	
            	restoreUndo();       
            }
    	}
      }
    
    if(key == 'f') {
  	  
      if(loaded == 1) {
       if(mouseOnScreen()){
    	  if (changing==false){
    	   storeUndo();
    	   changing=true;
       }
        if(view=='X') {
          floodFill(layer,floor((mouseY-blockSize*2)/blockSize),floor((mouseX-blockSize*2)/blockSize),level[layer][floor((mouseY-blockSize*2)/blockSize)][floor((mouseX-blockSize*2)/blockSize)][0]);
        }
        if(view=='Y') {
          floodFill(floor((mouseX-blockSize*2)/blockSize),layer,floor((mouseY-blockSize*2)/blockSize),level[floor((mouseX-blockSize*2)/blockSize)][layer][floor((mouseY-blockSize*2)/blockSize)][0]);
        }
        if(view=='Z') {
          floodFill(floor((mouseX-blockSize*2)/blockSize),floor((mouseY-blockSize*2)/blockSize),layer,level[floor((mouseX-blockSize*2)/blockSize)][floor((mouseY-blockSize*2)/blockSize)][layer][0]);
       
        }
       }
      }        
    }
    
    
    if(key == CODED) {
      if(keyCode == DOWN) {
        brush++;
        brush=brush%22;
      }
      if(keyCode == UP) {
        if(brush>0) {
          brush--;
        } else {
          brush = 21;
        }
      }
      if(keyCode == RIGHT) {
        team++;
        team=team%3;
      }
      if(keyCode == LEFT) {
        if(team>0) {
          team--;
        } else {
          team = 2;
        }
      }
      
       if(keyCode == 33) { //PAGEUP
            brushSize++;
            if (brushSize > 10) brushSize = 1;
       }
       if(keyCode == 34) { //PAGEDOWN
           brushSize--;
           if (brushSize <= 0) brushSize =10;
      }
    }
  } else {
    if(command == 1) {
      if(key != 10) {
        filename += key;
      } else {
        if(filename.length()==0) {
          command = 0;
        } else {
          command = 2;
        }
      }
      if(key == 8) {
        if(filename.length()!=1) {
          filename = filename.substring(0, filename.length()-2);
          println(filename.length());
        }
      }
    }
    if(command == 4) {
      if(key == 'y') {
        makeEmptyMap();
        command = 0;
      }
      if(key == 'n') {
        command = 0;
      }
    }
    if(command == 6) {
      if(key == 'y') {
        makeFlatMap();
        command = 0;
      }
      if(key == 'n') {
        command = 0;
      }
    }
    if(command == 9) {
        if(key == 'y') {
          convert();
          command = 0;
        }
        if(key == 'n') {
          command = 0;
        }
      }
 // Manage open map confirmation prompt
    if(command == 1) {
      if(key == 'y') {
        showOpenMapDialog();
      }
      if(key == 'n') {
        command = 0;
      }
    }
  }
}

//void mousePressed()
//Callback

public void mousePressed() {
  if(command==0) {
	//Undo  
	if (changing == false){
		storeUndo();
		changing=true;
	}
	  
    if(mouseButton == LEFT && loaded == 1 && mouseOnScreen()) {
      parseLevel(coord[0],coord[1],brush);
    }
    //Delete tile on right mouse click
    if(mouseButton == RIGHT && loaded == 1 && mouseOnScreen()) {
        parseLevel(coord[0],coord[1],0);
    }
  }
  if(command==5) {
    for(int i=0;i<buttonIndex;i++) {
      if(mouseX > buttons[i].x && mouseX < buttons[i].x+150 && mouseY > buttons[i].y && mouseY < buttons[i].y+30) {
        buttons[i].clicked();
      }
    }
  }
}

//void mouseDragged()
//Callback

public void mouseDragged() {
  mousePressed();
}

public void mouseReleased() {
	changing=false;
}
//
//FUNCTIONS
//

//void saveData()
//Arguments:  none
//Returns:    nothing
//Effect:     Saves the current map loaded in the 'level' array to a location specified by the global variable 'mapName'

public void saveData() {
  PrintWriter output = createWriter(mapName);
  
  for(int i=0;i<64;i++) {
    for(int j=0; j<64; j++) {
      for(int k=0; k<64; k++) {
        output.println(str(level[i][63-j][k][0])+","+str(level[i][63-j][k][1]));
      }
    }
  }
  
  output.flush();
  output.close();
  
  loaded = 1;
}

//void saveAs()
//Arguments:  none
//Returns:    nothing
//Effect:     Prompts the user for a new location and saves the current map there. 
//          As a side effect, further saves will go to this path, leaving the original file untouched.

public void saveAs() {
  String savePath = selectOutput();
  if (savePath != null) {
    filename = savePath;
    mapName = savePath;
    saveData();
  }
  command = 0;
}

//void makeEmptyMap()
//Arguments:  none
//Returns:    nothing
//Effect:     Prompts the user for a save location and initializes the 'level' array into an empty map, then saves.

public void makeEmptyMap() {
  String savePath = selectOutput();
  if (savePath != null) {
    filename = savePath;
    mapName = savePath;
    for(int i=0; i<64; i++) {
      for(int j=0; j<64; j++) {
        for(int k=0; k<64; k++) {
          level[i][j][k][0] = 0;
          level[i][j][k][1] = 0;
        }
      }
    }
    saveData();
  }
  clearUndo();
}

//void makeFlatMap()
//Arguments:  none
//Returns:    nothing
//Effect:     Prompts the user for a save location and fills the 'level' array up to the ground level, then saves.

public void makeFlatMap() {
  String savePath = selectOutput();
  if (savePath != null) {
    filename = savePath;
    mapName = savePath;
    for(int i=0; i<64; i++) {
      for(int j=0; j<64; j++) {
        for(int k=0; k<64; k++) {
          if(j<=8) {
            level[i][j][k][0] = 0;
          } else {
            level[i][j][k][0] = 1;
          }
          level[i][j][k][1] = 0;
        }
      }
    }
    saveData();
  }
  clearUndo();
}


//Convert Infiniminer 1.3 Maps to 1.4/1.5 Maps

public void convert(){
	   for(int i=0; i<64; i++) {
		      for(int j=0; j<64; j++) {
		    	  for(int k=0; k<64; k++) {
		    		  if (level[i][j][k][0] > 11 && level[i][j][k][0] < 19){
		    			  level[i][j][k][0] = level[i][j][k][0]+3;
		    		  }
		    	  }
		      }
	   }
	   clearUndo();
}

//void floodFill(int x, int y, int z, int b)
//Arguments:  int x: The global x coordinate from where to start
//      int y: The global y coordinate from where to start
//      int z: The global z coordinate from where to start
//      int b: The brush under the cursor, this represents the color we want to replace.
//Returns:    nothing
//Effect:     Starts a recursive 2D fill from the coordinates passed as arguments until it finds a change in the blocks

public void floodFill(int x, int y, int z, int b) {
	
	
  if(x < 64 && x >= 0 && y < 64 && y >= 0 && z >= 0 && z < 64) {
    if(level[x][y][z][0] == b && level[x][y][z][0] != brush) {
      level[x][y][z][0] = brush;
      level[x][y][z][1] = team;
      if(view=='X') {
        floodFill(x, y+1, z, b);
        floodFill(x, y-1, z, b);
        floodFill(x, y, z+1, b);
        floodFill(x, y, z-1, b);
      }
      if(view=='Y') {
        floodFill(x+1, y, z, b);
        floodFill(x-1, y, z, b);
        floodFill(x, y, z+1, b);
        floodFill(x, y, z-1, b);
      }
      if(view=='Z') {
        floodFill(x, y+1, z, b);
        floodFill(x, y-1, z, b);
        floodFill(x+1, y, z, b);
        floodFill(x-1, y, z, b);
      }
    }
  }
  
}

//void loadLevel(String path)
//Arguments:  String path
//Returns:    nothing
//Effect:     Saves the current level to the location specified by 'path' and sets the 'loaded' global variable to 1

public void loadLevel(String path) {
byte b[] = loadBytes(path);
int lineNum = 0;
int i = 0;
String parsed = "";

while(lineNum < 262144) {
  parsed = "";
  while(b[i] != ',') {
    parsed += PApplet.parseChar(b[i] & 0xff);
    i++;
  }
  level[ceil(lineNum/64/64)%64][63-ceil(lineNum/64)%64][lineNum%64][0] = PApplet.parseInt(parsed);
  
  i++;
  
  parsed = "";
  while(b[i] != 13) {
    parsed += PApplet.parseChar(b[i] & 0xff);
    i++;
  }
  level[ceil(lineNum/64/64)%64][63-ceil(lineNum/64)%64][lineNum%64][1] = PApplet.parseInt(parsed);
  
  i+=2;
  lineNum++;
}

println("Loaded "+lineNum+" lines");

loaded = 1;
clearUndo();
storeUndo();
undostep=0;
}

//parseLevel
//void parseLevel()
//Arguments:  int x, int y: starting coordinates
//			  int b: selected brush
//Returns:    nothing
//Effect:     puts the selected brush level[][][][] - Array

public void parseLevel(int x, int y, int b){
int sx,sy;
 if (brushtype == 1){ //SQUARE
   for( int i=0;i < (brushSize+brushSize-1);i++){
	   for( int j=0;j < (brushSize+brushSize-1);j++) {
		  sx=x-(brushSize-1);
		  sy=y-(brushSize-1);
		  if(sx+j >= 0 && sx+j <= 63 && sy+i >= 0 && sy+i <= 63){
		  if(view=='X') {
			  level[layer][sy+i][sx+j][0]=b;
			  level[layer][sy+i][sx+j][1]=team;
		  }
		  if(view=='Y') {
			  level[sx+j][layer][sy+i][0]=b;
			  level[sx+j][layer][sy+i][1]=team;
		  }
		  if(view=='Z') {
			  level[sx+j][sy+i][layer][0]=b;
			  level[sx+j][sy+i][layer][1]=team;
		  }
		  }
	   }
   }
 }
 //space for more brushtypes
}

//void drawCursor()
//Arguments:  none
//Returns:    nothing
//Effect:     draws the cursor on the editing screen respective to brushform and size

public void drawCursor(){
	int bs;
	bs=brushSize*2 -1;
	stroke(255);
	
	if (brushtype == 1) { //SQUARE
		rect(mouseX/blockSize*blockSize-(brushSize-1)*blockSize, mouseY/blockSize*blockSize-(brushSize-1)*blockSize, blockSize*bs, blockSize*bs);
	}
	//more space for other brushtypes
	noStroke();
}

//void manageCommands()
//Arguments:  none
//Returns:    nothing
//Effect:     Performs specific actions according to the value of the global variable 'command'

public void manageCommands() {
	switch(command) {
	case 1:  // Load map
		if(loaded != 0) {
			fill(0);
			text("Are you sure you want to close the current map? (y / n)", blockSize*2, 748);
		} else {
			showOpenMapDialog();
		}
		break;
	case 2:  // Load
	case 3:  // Save
		fill(255);
		rect(blockSize*2+1, blockSize*2 + 29*blockSize, blockSize*64-1, blockSize*3);
		fill(0);
		text("Please wait... ", blockSize*2 + 31*blockSize, blockSize*2 + 31*blockSize);
		loaded = command;
		command = 0;
		break;
	case 4:  // New blank map
		text("Are you sure you want to create an EMPTY map? (y / n)", blockSize*2, 748);
		break;
	case 6:  // New empty map
		text("Are you sure you want to create a FLAT map? (y / n)", blockSize*2, 748);
		break;
	case 5:  // Show menu
		fill(150, 150, 150, 50);
		rect(blockSize*2 + blockSize*29 - 50, 100, 200, 400);
		fill(100);
		rect(blockSize*2 + blockSize*29 - 50, 100, 200, 400);
		for(int i=0; i<buttonIndex; i++) {
			buttons[i].display();
		}
		break;
	case 7: // Save as
		saveAs();
		break;	
	case 9:  // convert
    text("Are you sure you want to convert the MAP? (y / n)", blockSize*2, 748);
    break;
}
}
	
//void showOpenMapDialog()
//Arguments:  none
//Returns:    nothing
//Effect:     Asks the user for a location and starts the load command. 
//          If the user cancelled the operation, 'filename' is restored 
//          to the current loaded map location and 'command' is set to 0.

public void showOpenMapDialog() {
	filename = selectInput();
	if (filename == null) {
		filename = mapName;
		loaded = 0;
		command = 0;
	} else {
		command = 2;
	}
}

//void displayGroundLevelIndicator()
//Arguments:  none
//Returns:    nothing
//Effect:     Shows the user where is the ground level in the current layer and axis

public void displayGroundLevelIndicator() {
	if (borderline && loaded == 1){
		  stroke(255, 0, 0);
		  rect(blockSize*3, blockSize*3, blockSize*62, blockSize*62);
		  stroke(255);
		  fill(255, 0, 0);
		  text("Level boundary", blockSize*2 + 15, blockSize*3 - 1);
		  noStroke();  
	  }
	if((view=='X' || view=='Z') && loaded == 1) {
		stroke(255, 110, 110);
		line(blockSize*2, blockSize*11, blockSize*66, blockSize*11);
		stroke(255);
		fill(255, 110, 110);
		text("Ground level", blockSize*3 + 5, blockSize*11 - 2);
		noStroke();
	}

	if(view == 'Y' && layer == 9 && loaded == 1) {
		fill(255, 20, 20);
		rect(blockSize*2-1, 0, blockSize*10, blockSize*2);
		fill(0);
		text("Ground level", blockSize*2 + 5, blockSize+blockSize/2);
		mapBorderColor = color(255, 20, 20);
	} else {
		mapBorderColor = color(0, 0, 0);
	}
	
}

public void storeUndo(){
	if (view=='Z'){
		for(int i=0; i<64;i++){
			for(int j=0; j<64;j++){
				undo[i][j][0][undostep]=level[i][j][layer][0];
				undo[i][j][1][undostep]=level[i][j][layer][1];
				undocontrol[undostep][0]=layer;
				undocontrol[undostep][1]= 2; // for Z
			}
		}
	
	}
	if (view=='Y'){
		for(int i=0; i<64;i++){
			for(int j=0; j<64;j++){
				undo[i][j][0][undostep]=level[i][layer][j][0];
				undo[i][j][1][undostep]=level[i][layer][j][1];
				undocontrol[undostep][0]=layer;
				undocontrol[undostep][1]= 1; // for Y
			}
		}
	
	}
	if (view=='X'){
		for(int i=0; i<64;i++){
			for(int j=0; j<64;j++){
				undo[i][j][0][undostep]=level[layer][i][j][0];
				undo[i][j][1][undostep]=level[layer][i][j][1];
				undocontrol[undostep][0]=layer;
				undocontrol[undostep][1]= 0; // for X
			}
		}
	
	}
	undostep++;
	//listhandling
	if (undostep > 10){
		undostep=10;
		for(int k=0;k <10;k++){
			for(int i=0; i<64;i++){
				for(int j=0; j<64;j++){
					undo[i][j][0][k]=undo[i][j][0][k+1];
					undo[i][j][1][k]=undo[i][j][1][k+1];
					undocontrol[k][0]=undocontrol[k+1][0];
					undocontrol[k][1]=undocontrol[k+1][1];
				}
			}
		}
	}
	maxundo=undostep;
}

public void restoreUndo(){
	if (undocontrol[undostep][1]== 2){ //for Z-View
		for(int i=0; i<64;i++){
			for(int j=0; j<64;j++){
				level[i][j][undocontrol[undostep][0]][0]=undo[i][j][0][undostep];
				level[i][j][undocontrol[undostep][0]][1]=undo[i][j][1][undostep];			
			}
		}
	
	}
	if (undocontrol[undostep][1]== 1){ //for Y-View
		for(int i=0; i<64;i++){
			for(int j=0; j<64;j++){
				level[i][undocontrol[undostep][0]][j][0]=undo[i][j][0][undostep];
				level[i][undocontrol[undostep][0]][j][1]=undo[i][j][1][undostep];			
			}
		}
	
	}
	if (undocontrol[undostep][1]== 0){ //for X-View
		for(int i=0; i<64;i++){
			for(int j=0; j<64;j++){
				level[undocontrol[undostep][0]][i][j][0]=undo[i][j][0][undostep];
				level[undocontrol[undostep][0]][i][j][1]=undo[i][j][1][undostep];							
			}
		}
	
	}

}

public void clearUndo(){
	undo = new int[64][64][2][11];
	undocontrol = new int[11][2];
	undostep=0;
	maxundo=0;
	
}

//boolean mouseOnScreen()
//Arguments:  none
//Returns:    true if mouse is on editing screen and false when not
//Effect:     Just a check if the mouse is on the editing screen

public boolean mouseOnScreen(){
	if(mouseX>=blockSize*2 && mouseY>=blockSize*2 && mouseX<blockSize*2 + blockSize*64 && mouseY<blockSize*2 + blockSize*64) {
	return true;
	}else{
	return false;
	}
}

//
//CLASSES
//

class MouseWheelInput implements MouseWheelListener{  
	  
	  public void mouseWheelMoved(MouseWheelEvent e) {
	   int totalScrollAmount = e.getWheelRotation() ;
	    if (scrolled) {
	    	scrolled=false;
	    brush+=totalScrollAmount;
	    
	    if(brush>21) {
	      brush = 21;
	    }
	    if(brush<0) {
	      brush = 0;
	    }
	    
	    } else {
	    	scrolled =true;
	    }
	  }
	  
	} 

class Button extends Object {
	  int x, y;
	  int c;
	  String caption;
	  int requireLoadedMap;
	  
	  //  void Button(String t, int xpos, int ypos, int com, int l)
	  //  Constructor
	  //  Arguments:  String t: The caption that will be shown on top of the button
	  //              int xpos: The x position of the button
	  //              int ypos: The y position of the button
	  //              int com:  The command that will be triggered when user clicks the button
	  //              int l:    A flag that specifies if a map must be loaded for the button to be enabled. May take 1 or 0.
	  //  Returns:    nothing
	  //  Effect:     Creates a new button and initializes it
	  
	  Button(String t, int xpos, int ypos, int com, int l) {
	    x = xpos;
	    y = ypos;
	    c = com;
	    caption = t;
	    requireLoadedMap = l;
	  }
	  
	  //  void clicked()
	  //  Arguments:  none
	  //  Returns:    nothing
	  //  Effect:     Called when the button is clicked (see events section). Sets global variable 'command' to the button's command code.
	  
	  public void clicked() {
	    if((requireLoadedMap == 1 && loaded == 1) || requireLoadedMap == 0) {
	      command = c;
	    }
	  }
	  
	  //  void display()
	  //  Arguments:  none
	  //  Returns:    nothing
	  //  Effect:     Displays the button at the location specified by the class variables x and y
	  
	  public void display() {
	    if((requireLoadedMap == 1 && loaded == 1) || requireLoadedMap == 0) {
	      fill(180);
	      rect(x-1, y-1, 152, 30);
	      fill(150);
	      rect(x, y, 150, 30);
	      fill(255);
	      text(caption, x+10, y+20);
	    } else {
	      fill(100);
	      rect(x-1, y-1, 152, 30);
	      fill(150);
	      rect(x, y, 150, 30);
	      fill(200);
	      text(caption, x+10, y+20);
	    }
	  }
	}

 

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#EBE9ED", "Infinimeditor" });
  }
}
