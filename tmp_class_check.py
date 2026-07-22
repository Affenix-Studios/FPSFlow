import zipfile, pathlib
jar = pathlib.Path('build/java25/libs/fpsflow-1.8-mc1.21.11-java25.jar')
print('exists', jar.exists())
with zipfile.ZipFile(jar, 'r') as z:
    for name in z.namelist():
        if name.endswith('.class'):
            data = z.read(name)
            if b'class_1297' in data:
                idx = data.index(b'class_1297')
                print(name, idx)
                print(data[max(0, idx-40):idx+120])
