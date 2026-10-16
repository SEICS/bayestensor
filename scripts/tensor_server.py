import socket
import sys
import numpy as np
import traceback
import ast

def multiline_eval(expr, context):
    "Evaluate several lines of input, returning the result of the last line"
    tree = ast.parse(expr)
    eval_expr = ast.Expression(tree.body[-1].value)
    exec_expr = ast.Module(tree.body[:-1], type_ignores=[])
    exec(compile(exec_expr, 'file', 'exec'), context)
    return eval(compile(eval_expr, 'file', 'eval'), context)

def recvall(sock):
    BUFF_SIZE = 4096 * 1024 # 4 MB
    data = b''
    while True:
        part = sock.recv(BUFF_SIZE)
        data += part
        if len(part) < BUFF_SIZE:
            # either 0 or end of data
            break
    return data
END_MARK = '<END>"'
END_SIZE = len(END_MARK)
def recvfull(sock):
	out = b''

	while True:
		line = recvall(sock)
		sline = str(out + line)
		print('received <%d>' % len(sline), file=sys.stderr)
		# print('received <%s>' % sline, file=sys.stderr)
		# print('endswith "%s"' % str(sline.endswith(END_MARK)), file=sys.stderr)
		if(sline.endswith(END_MARK)):
		# if(not line):
			out += line
			out = out[0:-END_SIZE]
			break
		if(line == b''):
			break
		out += line
		pass
	return out

if __name__ == "__main__":
	if(len(sys.argv) != 1):
		print('Usage: tensor_server.py')
		sys.exit(1)
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	server_address = ('localhost', 10042)
	print('starting up on %s port %s' % server_address, file=sys.stderr)
	sock.bind(server_address)
	sock.listen(1)

	while True:
		connection, client_address = sock.accept()
		print('connection from ', client_address, file=sys.stderr)

		try:
			# while True:
				# line = connection.recv(1000 * 1000 * 1000)
				line = recvfull(connection)
				# print('received "%s"' % line, file=sys.stderr)
				# if(line == b''):
				# 	break
				# k = 10
				# output_words = evaluateDistribution(input_lang, output_lang, encoders, decoder, line, k)
				# outputLine = ''
				# for i in range(len(output_words)):
				# 	outputLine += "%s,%.2f;" % (output_words[i][0], output_words[i][1])
				# outputLine = ' '
				outputLine = ''
				try:
					res = multiline_eval(line.decode(), None)
					outputLine += str(res) + '\n'
					# outputLine += eval() + '\n'
					connection.sendall(outputLine.encode())
					print('sent: ' + outputLine)
				except Exception as e:
					traceback.print_exc()
					print("[INTERNAL ERROR] ", e, file=sys.stderr)
				pass
		# except (socket.error, msg):
		# 	sys.stderr.write("[ERROR] %s\n" % msg[1])
		except Exception as e:
			traceback.print_exc()
			print("[ERROR] ", e, file=sys.stderr)
		finally:
			connection.close()
	# sock.shutdown(1)
	sock.close()