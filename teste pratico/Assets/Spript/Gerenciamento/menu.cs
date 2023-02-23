using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class menu : MonoBehaviour
{
    public static float _valorTime, _spawnInim;
    public bool         _opcoes;
    public GameObject   _MenuOpcoes; 

    void Start()
    {
        _valorTime = 60;
        _spawnInim = 3;
    }

    
    void Update()
    {
        if(_opcoes)
        {
            _MenuOpcoes.SetActive(true);
        } else
        {
            _MenuOpcoes.SetActive(false);
        }
    }
    //Tempo de partida 1 min
    public void temp60()
    {
        _valorTime = 60;
    }
    //tempo de partida 2 min
    public void temp120()
    {
        _valorTime = 120;
    }
    //tempo de partida 3 min
    public void temp180()
    {
        _valorTime = 180;
    }
    //botao para iniciar o jogo
    public void inicar()
    {
        SceneManager.LoadScene("SampleScene");        
    }
    //botao para abrir o minuto
    public void botaoOpcao()
    {
        _opcoes = !_opcoes;
    }
    //tempo de spawn do inimigos 3 segundos
    public void tempSpawn1()
    {
        _spawnInim = 3;
    }
    //tempo de spawn do inimigos 4 segundos
    public void tempSpawn2()
    {
        _spawnInim = 4;
    }
    //tempo de spawn do inimigos 5 segundos
    public void tempSpawn3()
    {
        _spawnInim = 5;
    }
    //sair do jogo
    public void quit()
    {
        Application.Quit();
    }

}
