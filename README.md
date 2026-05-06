🤖 RoboDetect

Sistema mobile desenvolvido para aprimorar a avaliação de robôs em competições da Olimpíada Brasileira de Robótica (OBR), utilizando técnicas de visão computacional em tempo real.

📖 Sobre o Projeto
Sistema desenvolvido durante pesquisas para o TCC do curso Sistemas para Internet - Intituto Federal De Ciência e Tecnologia do Rio Grande do Sul (IFRS) 2025/2
Autor: Juliano Gomes

O RoboDetect foi criado com o objetivo de reduzir a subjetividade nas avaliações realizadas durante a modalidade Resgate da OBR, fornecendo uma prova visual objetiva e confiável do trajeto percorrido pelos robôs.

A solução realiza a sobreposição entre o trajeto ideal da pista e o caminho executado pelo robô, permitindo análise precisa e auditável.

🎯 Objetivos
Reduzir contestações durante as provas
Aumentar a precisão da avaliação
Gerar evidências visuais confiáveis
Auxiliar juízes na conferência de pontuação

🛠️ Tecnologias
Tecnologia	Descrição
Java	Linguagem principal do aplicativo
Android SDK	Plataforma de desenvolvimento mobile
OpenCV 4.12.0	Biblioteca de visão computacional

👁️ Técnicas Utilizadas
Detecção de contornos (Mat, MatOfPoint)
Subtração de fundo (BackgroundSubtractorMOG2)
Conversão para escala de cinza
Segmentação de imagem
Processamento em tempo real
Sobreposição de trajetos

📱 Funcionalidades

🔐 Autenticação

Login com e-mail e senha

Cadastro de usuários

🏁 Gerenciamento de Pistas

Upload de imagens

Armazenamento local

🧭 Processamento de Pista

Conversão para grayscale

Detecção de contornos

Destaque do trajeto ideal

📷 Captura de Trajeto

Uso da câmera em tempo real

Identificação do robô

Desenho do percurso (linha verde)

Sobreposição com a pista (linha vermelha)

📊 Avaliação
Geração de imagem final
Comparação visual de desempenho

⚙️ Como Funciona
1. Login no sistema
2. Cadastro ou seleção de pista
3. Posicionamento do dispositivo
4. Início da captura
5. Processamento em tempo real
6. Geração da imagem final
7. Associação com o robô avaliado

📦 Estrutura do Projeto
RoboDetect/

│── app/

│   ├── src/

│   │   ├── main/

│   │   │   ├── java/        # Código-fonte

│   │   │   ├── res/         # Layouts e recursos

│   │   │   └── AndroidManifest.xml

│── OpenCV/

│── build.gradle

│── README.md

⚠️ Requisitos
Android 8.0 ou superior

Câmera funcional

Iluminação no ambiente de utilização suficiente para a câmera capturar o robô

Dispositivo fixo (uso de suporte recomendado)

⚠️ Movimentações no dispositivo comprometem a precisão da análise.

📈 Benefícios

Avaliação mais precisa
Redução de erros humanos
Registro auditável
Suporte à decisão dos juízes

🔮 Melhorias Futuras

Cálculo automático de pontuação
Integração com sistemas da OBR
Uso de IA para análise avançada
Armazenamento em nuvem
Dashboard de resultados

📸 Demonstração

Telas de login e cadastro de usuário
<img width="240" height="228" alt="image" src="https://github.com/user-attachments/assets/66f1244c-513b-439e-8d35-f69adb274a18" />

 Tela de menu do aplicativo
<img width="163" height="354" alt="image" src="https://github.com/user-attachments/assets/8bd59e5c-f09d-41ba-9fe6-6d79db9c65a5" />

Tela gerenciamento de pistas
<img width="143" height="226" alt="image" src="https://github.com/user-attachments/assets/e86de298-17f5-4fd1-82aa-1607e1580b89" />

Pista processada
<img width="436" height="192" alt="image" src="https://github.com/user-attachments/assets/f5178c76-161c-4ce2-9771-f0081c0e74ea" />

Trajeto capturado
<img width="282" height="127" alt="image" src="https://github.com/user-attachments/assets/cbd73abb-952c-486b-b39e-749e13ddc910" />

Pista e trajeto sobrepostos e salvos para avaliação
<img width="136" height="231" alt="image" src="https://github.com/user-attachments/assets/928b5c1c-1579-4471-8037-e224525c5ba2" />


🚀 Como Executar
# Clone o repositório
git clone https://github.com/seu-usuario/robodetect.git

# Abra no Android Studio
# Configure o OpenCV
# Execute em um dispositivo físico
