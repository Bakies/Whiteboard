from tkinter import *
import socket 
from tkinter.colorchooser import askcolor


class Paint(object):

    DEFAULT_PEN_SIZE = 5.0
    DEFAULT_COLOR = 'black'

    def __init__(self):
        self.port = 15272
        self.serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.serverSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.serverSocket.bind(('0.0.0.0', self.port))
        self.root = Tk()

        self.penButton = Button(self.root, text='pen', command=self.usePen)
        self.penButton.grid(row=0, column=0)

        self.colorButton = Button(self.root, text='color', command=self.chooseColor)
        self.colorButton.grid(row=0, column=2)

        self.eraserButton = Button(self.root, text='eraser', command=self.useEraser)
        self.eraserButton.grid(row=0, column=3)

        self.sizeButton = Scale(self.root, from_=1, to=10, orient=HORIZONTAL)
        self.sizeButton.grid(row=0, column=4)

        self.canvas = Canvas(self.root, bg='white', width=600, height=600)
        self.canvas.grid(row=1, columnspan=5)

        self.setup()
        self.root.mainloop()
        self.listen()

    def setup(self):
        self.oldX = None
        self.oldY = None
        self.lineWidth = self.sizeButton.get()
        self.color = self.DEFAULT_COLOR
        self.eraserOn = False
        self.activateButton = self.penButton
        self.canvas.bind('<B1-Motion>', self.paint)
        self.canvas.bind('<ButtonRelease-1>', self.reset)

    def usePen(self):
        self.activateButton(self.penButton)

    def chooseColor(self):
        self.eraserOn = False
        self.color = askcolor(color=self.color)[1]

    def useEraser(self):
        self.activateButton(self.eraser_button, eraser_mode=True)

    def activateButton(self, some_button, eraser_mode=False):
        self.activateButton.config(relief=RAISED)
        some_button.config(relief=SUNKEN)
        self.activateButton = some_button
        self.eraserOn = eraser_mode

    def paint(self, event):
        self.lineWidth = self.sizeButton.get()
        paintColor = 'white' if self.eraserOn else self.color
        if self.oldX and self.oldY:
            self.canvas.create_line(self.oldX, self.oldY, event.x, event.y,
                               width=self.lineWidth, fill=paintColor,
                               capstyle=ROUND, smooth=TRUE, splinesteps=36)
        self.oldX = event.x
        self.oldY = event.y

    def reset(self, event):
        self.oldX, self.oldY = None, None

    # Accepts new sockets from server socket
    def listen(self):
        pass
    # Recv data from client and translate that to lines in the Tk app
    def listenToClient(self, client, address):
        pass


if __name__ == '__main__':
    Paint()

