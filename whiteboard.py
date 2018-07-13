from tkinter import *
import socket 
from tkinter.colorchooser import askcolor


class Paint(object):

    DEFAULT_PEN_SIZE = 5.0
    DEFAULT_COLOR = 'black'

    def __init__(self):
        self.port = 15272
        self.serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.bind(('0.0.0.0', self.port))
        self.root = Tk()

        self.penButton = Button(self.root, text='pen', command=self.use_pen)
        self.penButton.grid(row=0, column=0)

        self.colorButton = Button(self.root, text='color', command=self.choose_color)
        self.colorButton.grid(row=0, column=2)

        self.eraserButton = Button(self.root, text='eraser', command=self.use_eraser)
        self.eraserButton.grid(row=0, column=3)

        self.sizeButton = Scale(self.root, from_=1, to=10, orient=HORIZONTAL)
        self.sizeButton.grid(row=0, column=4)

        self.canvas = Canvas(self.root, bg='white', width=600, height=600)
        self.canvas.grid(row=1, columnspan=5)

        self.setup()
        self.root.mainloop()
        self.listen()

    def setup(self):
        self.old_x = None
        self.old_y = None
        self.line_width = self.choose_size_button.get()
        self.color = self.DEFAULT_COLOR
        self.eraser_on = False
        self.active_button = self.pen_button
        self.canvas.bind('<B1-Motion>', self.paint)
        self.canvas.bind('<ButtonRelease-1>', self.reset)

    def use_pen(self):
        self.activate_button(self.pen_button)

    def choose_color(self):
        self.eraser_on = False
        self.color = askcolor(color=self.color)[1]

    def use_eraser(self):
        self.activate_button(self.eraser_button, eraser_mode=True)

    def activate_button(self, some_button, eraser_mode=False):
        self.active_button.config(relief=RAISED)
        some_button.config(relief=SUNKEN)
        self.active_button = some_button
        self.eraser_on = eraser_mode

    def paint(self, event):
        self.line_width = self.choose_size_button.get()
        paint_color = 'white' if self.eraser_on else self.color
        if self.old_x and self.old_y:
            self.canvas.create_line(self.old_x, self.old_y, event.x, event.y,
                               width=self.line_width, fill=paint_color,
                               capstyle=ROUND, smooth=TRUE, splinesteps=36)
        self.old_x = event.x
        self.old_y = event.y

    def reset(self, event):
        self.old_x, self.old_y = None, None

    # Accepts new sockets from server socket
    def listen(self):
        pass
    # Recv data from client and translate that to lines in the Tk app
    def listenToClient(self, client, address):
        pass


if __name__ == '__main__':
    Paint()

