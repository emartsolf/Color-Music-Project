/*

  Client audio script - auditions the correct sample when client selects from bottom selection bar

*/

// MIDI note constants
/*
60 => int c;     72 => int C;
61 => int cs;    73 => int Cs;
62 => int d;     74 => int D;
63 => int ds;    75 => int Ds;
64 => int e;     76 => int E;
65 => int f;     77 => int F;
66 => int fs;    78 => int Fs;
67 => int g;     79 => int G;
68 => int gs;    80 => int Gs;
69 => int a;     81 => int A;
70 => int as;    82 => int As;
71 => int b;     83 => int B;
*/
// https://chuck.cs.princeton.edu/release/files/chuck_manual.pdf
// ^^^ StkInstrument for timbre!!! ^^^

6660 => int inPort;
-1 => int id; 

if( me.args() ) me.arg(0) => Std.atoi => id;
if( me.args() ) me.arg(1) => Std.atoi => inPort;

OscIn oin;
OscMsg msg;
inPort => oin.port;
oin.addAddress( "/grid/selection, ii" );


// SndBuf s[12];
// 0 => int sCounter;

// JCRev r => Gain g => dac;

SinOsc s => Gain g => dac;

/*
for( int i; i < s.cap(); i++ ) { 
    s[i] => r; 
    0 => s[i].gain; 
}

.01 => r.mix;

16.0 => float userCount;
*/

// string sampleName[9];
// int sampleset;
    
// call this if id has changed when OSC message is received
fun void setup(int _id)
{
    // This could receive the color of the palatte and assign the scale that goes with it eventually
    // For now, assume _id = value
}

0 => int flag;

int currentClientId, currentValue;

while(true)
{
    oin => now;
       
    if(flag==0) {
        <<< "[grid] OSC input received..." >>>; 
        1 => flag;
    }
    
    // get message
    
    // grab the next message from the queue. 
    while( oin.recv(msg) )
    {             
        // /oscMultiGrid <port> <client-id> <current-cell> <value>
        // ^^ I'd like to receive <note being played> <row>
        // @row: instrument to play the note on
        msg.getInt(0) => id;
        msg.getInt(1) => value;
        msg.getInt(2) => row;

        // reset sample set if client id has changed - will run on first selection too with default -1 val
        // if( id != currentClientId )
        //    setup(currentClientId);
        
        if( row == 1 )
            spork ~ play(value);
        else if( row == 2 )
            spork ~ playFlute(value);
        else if( row == 3 )
            spork ~ playStrings(value);
        else if( row == 4 )
            spork ~ playBrass(value);
    }
}

// @value: note to play
fun void play( int value )
{
    SinOsc s => Gain g => dac;
    0.1 => g.gain;
    Std.mtof(value) => s.freq;
                    
    // me.dir() + sampleName[value] => s[sCounter].read;
        
    1::day => now;
}

fun void playBrass( int value ) {
    // Playing a PM UG trumpet in a nice space
    Brass trump => NRev rev => dac;
    0.1 => rev.mix;

    0.8 => trump.startBlowing;
    Std.mtof(value) => trump.freq;
    1 :: day => now;
}

1::day => now;