// *** For UART/USART functions to be used - Library files (e.g. "stm32f10x_usart.c") must be added to the project files/folders.
// *** In Configuration Flash Tools -> C/C++ The folder of the library files must be Defined (in this case, "USE_STDPERIPH_DRIVER" <- "StdPeriph" being the folder where the library files are located in).
// *** Below in "Include Paths" - Add the folders in which the files used for this project are located in.
#include "stm32f10x.h"
#include "stm32f10x_usart.h"
#include "stm32f10x_rcc.h"
#include "stm32f10x_gpio.h"
#include "misc.h"

int i; // Declare variable for the most recent received data by the USARTx peripheral.
  
//ErrorStatus HSEStartUpStatus;

// User defined function prototypes 
void NVIC_Configuration(void);
void GPIO_Configuration(void);
void USART_Configuration(void);
void USART1_IRQHandler(void);
void UARTSend(const unsigned char *pucBuffer, unsigned long ulCount);
  
//// *** ////
  
/******************************************************************************/
/*            STM32F10x Peripherals Interrupt Handlers                        */
/******************************************************************************/
  
/**
  * @brief  This function handles USARTx global interrupt request
  * @param  None
  * @retval None
  */
void USART1_IRQHandler(void)
{
		// If USART1 status regsiter & received data register are not reseted (not empty/null).
    if ((USART1->SR & USART_FLAG_RXNE) != (u16)RESET) 				// USART_SR =  (Status Register) register. | USART_FLAG_RXNE = Receive data register not empty flag. | (u16) = unsigned 16-bit integer number.
    {
        i = USART_ReceiveData(USART1);					// Initialise "i" variable with the received data by the USARTx peripheral.
			/*
        if(i == '1'){
            GPIO_WriteBit(GPIOA,GPIO_Pin_8,Bit_SET);        // Set '1' on PA8
					
						GPIO_WriteBit(GPIOA,GPIO_Pin_12,Bit_RESET);        // Set '0' on PA12
					
					
						GPIO_WriteBit(GPIOA,GPIO_Pin_1,Bit_SET);        // Set '1' on PA1
						GPIO_WriteBit(GPIOA,GPIO_Pin_0,Bit_RESET);        // Set '0' on PA0
					
					
            UARTSend("LED ON\r\n",sizeof("LED ON\r\n"));    // Send message to UART1
        }
        else if(i == '0'){
            GPIO_WriteBit(GPIOA,GPIO_Pin_8,Bit_RESET);      // Set '0' on PA8
					
					
						GPIO_WriteBit(GPIOA,GPIO_Pin_1,Bit_RESET);      // Set '0' on PA1
					
					
            UARTSend("LED OFF\r\n",sizeof("LED OFF\r\n"));
        }
				*/
				if(i == '0') {
					GPIO_WriteBit(GPIOA,GPIO_Pin_8,Bit_RESET);       	 // Set '0' on PA8
					GPIO_WriteBit(GPIOA,GPIO_Pin_1,Bit_RESET);        // Set '0' on PA1
					GPIO_WriteBit(GPIOA,GPIO_Pin_12,Bit_RESET);        // Set '0' on PA12
					GPIO_WriteBit(GPIOA,GPIO_Pin_0,Bit_RESET);        // Set '0' on PA0
				}
				else if(i == '1'){ 
					GPIO_WriteBit(GPIOA,GPIO_Pin_8,Bit_SET);       	 // Set '0' on PA8
					GPIO_WriteBit(GPIOA,GPIO_Pin_1,Bit_RESET);        // Set '0' on PA1
					GPIO_WriteBit(GPIOA,GPIO_Pin_12,Bit_RESET);        // Set '1' on PA12
					//GPIO_WriteBit(GPIOA,GPIO_Pin_0,Bit_SET);        // Set '0' on PA0
				}
				else if(i == '2'){					
					GPIO_WriteBit(GPIOA,GPIO_Pin_8,Bit_SET);       	 // Set '1' on PA8
					GPIO_WriteBit(GPIOA,GPIO_Pin_1,Bit_SET);        // Set '1' on PA1
					GPIO_WriteBit(GPIOA,GPIO_Pin_12,Bit_RESET);        // Set '0' on PA12
					GPIO_WriteBit(GPIOA,GPIO_Pin_0,Bit_RESET);        // Set '0' on PA0
				}
				else if(i == '3'){ 
					GPIO_WriteBit(GPIOA,GPIO_Pin_8,Bit_RESET);       	 // Set '0' on PA8
					GPIO_WriteBit(GPIOA,GPIO_Pin_1,Bit_RESET);        // Set '0' on PA1
					GPIO_WriteBit(GPIOA,GPIO_Pin_12,Bit_SET);        // Set '1' on PA12
					GPIO_WriteBit(GPIOA,GPIO_Pin_0,Bit_SET);        // Set '1' on PA0
				}
				else if(i == '4'){ 
					GPIO_WriteBit(GPIOA,GPIO_Pin_8,Bit_RESET);       	 // Set '0' on PA8
					GPIO_WriteBit(GPIOA,GPIO_Pin_1,Bit_SET);        // Set '0' on PA1
					//GPIO_WriteBit(GPIOA,GPIO_Pin_12,Bit_SET);        // Set '0' on PA12
					GPIO_WriteBit(GPIOA,GPIO_Pin_0,Bit_RESET);        // Set '1' on PA0
				}	
				
    }
}
  
void usart_rxtx(void)
{
    const unsigned char welcome_str[] = " Welcome to Bluetooth!\r\n";
  
    /* Enable USART1 and GPIOA clock */	
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1 | RCC_APB2Periph_GPIOA, ENABLE);
		/* Enable USART2 and GPIOA clock */	
    ///RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1 | RCC_APB2Periph_GPIOA, ENABLE);
  
    /* NVIC Configuration */
    NVIC_Configuration();
  
    /* Configure the GPIOs */
    GPIO_Configuration();
  
    /* Configure the USART1 */
    USART_Configuration();
  
    /* Enable the USART1 Receive interrupt: this interrupt is generated when the
         USART1 receive data register is not empty */
    USART_ITConfig(USART1, USART_IT_RXNE, ENABLE); 				
  
    /* print welcome information */
    UARTSend(welcome_str, sizeof(welcome_str));
}
  
/*******************************************************************************
* Function Name  : GPIO_Configuration
* Description    : Configures the different GPIO ports
*******************************************************************************/
void GPIO_Configuration(void)
{
  GPIO_InitTypeDef GPIO_InitStructure; 					// Declare GPIO_InitTypeDef variable for configuring GPIO port pins.
  
  /* Configure (PA.8) as output */
	// *** PA8 for this STM32F10x USART1_CK <- Found through Schematics/Datasheet/Documentation.
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_8; // Initialise Pins.
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz; //  GPIO speed
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_Out_PP; // Set GPIO mode as Output for specified pins. Output type "PP" is push-pull. <- Push-Pull = A transistor connects to high, and a transistor connects to low (only one is operated at a time).
  GPIO_Init(GPIOA, &GPIO_InitStructure); // Save
  
  /* Configure USART1 Tx (PA.09) as alternate function push-pull */
	// *** PA9 for this STM32F10x USART1_TX <- Found through Schematics/Datasheet/Documentation.
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP; // Set GPIO mode as Alternating Function - to use with peripheral (USART). Pin Type "PP" is push-pull.
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
  
  /* Configure USART1 Rx (PA.10) as input floating */
	// *** PA10 for this STM32F10x USART1_RX <- Found through Schematics/Datasheet/Documentation.
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING; // Set GPIO mode as Input. Input Pin Type is Floating. <- Floating = Pin in high impedance input mode, its state is indeterminate unless it is driven high or low by an external source. 
  GPIO_Init(GPIOA, &GPIO_InitStructure);
	
	
	/* Configure (PA.12) as output */
	// *** PA12 for this STM32F10x USART1_RTS <- Found through Schematics/Datasheet/Documentation.
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_12; // Initialise Pins.
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz; //  GPIO speed
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_Out_PP; // Set GPIO mode as Output for specified pins. Output type "PP" is push-pull. <- Push-Pull = A transistor connects to high, and a transistor connects to low (only one is operated at a time).
  GPIO_Init(GPIOA, &GPIO_InitStructure); // Save	
	
	
	
	/* Configure (PA.1) as output */
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_1; // Initialise Pins.
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz; //  GPIO speed
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_Out_PP; // Set GPIO mode as Output for specified pins. Output type "PP" is push-pull. <- Push-Pull = A transistor connects to high, and a transistor connects to low (only one is operated at a time).
  GPIO_Init(GPIOA, &GPIO_InitStructure); // Save
	
	/* Configure (PA.0) as output */
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_0; // Initialise Pins.
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz; //  GPIO speed
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_Out_PP; // Set GPIO mode as Output for specified pins. Output type "PP" is push-pull. <- Push-Pull = A transistor connects to high, and a transistor connects to low (only one is operated at a time).
  GPIO_Init(GPIOA, &GPIO_InitStructure); // Save
	
}
  
/*******************************************************************************
* Function Name  : USART_Configuration
* Description    : Configures the USART1
*******************************************************************************/
void USART_Configuration(void)
{
  USART_InitTypeDef USART_InitStructure;
  
/* USART1 configuration ------------------------------------------------------*/
  USART_InitStructure.USART_BaudRate = 9600;        // Baud Rate
  USART_InitStructure.USART_WordLength = USART_WordLength_8b;
  USART_InitStructure.USART_StopBits = USART_StopBits_1;
  USART_InitStructure.USART_Parity = USART_Parity_No;
  USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
  USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
  
  USART_Init(USART1, &USART_InitStructure); 				// Save
  
  // Enable USART1 
  USART_Cmd(USART1, ENABLE); 				
}
  
/**
  * @brief  Configures the nested vectored interrupt controller.
  * @param  None
  * @retval None
  */
void NVIC_Configuration(void)
{
  NVIC_InitTypeDef NVIC_InitStructure;
  
  /* Enable the USARTx Interrupt */
  NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&NVIC_InitStructure);
}
  
/*******************************************************************************
* Function Name  : UARTSend
* Description    : Send a string to the UART.
* Input          : - pucBuffer: buffers to be printed.
*                : - ulCount  : buffer's length
*******************************************************************************/
void UARTSend(const unsigned char *pucBuffer, unsigned long ulCount)
{
    //
    // Loop while there are more characters to send.
    //
    while(ulCount--)
    {
        USART_SendData(USART1, (uint16_t) *pucBuffer++); 				// Transmits single data through the USARTx peripheral.
        /* Loop until the end of transmission */
        while(USART_GetFlagStatus(USART1, USART_FLAG_TC) == RESET) 				// Loop while the specified USART is reset. USART_GetFlagStatus = Checks whether the specified USART flag is set or not.
        {
        }
    }
}


int main(void)
{
     usart_rxtx();    while(1)
    {
  
    }
}
