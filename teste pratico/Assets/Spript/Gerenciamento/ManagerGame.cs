using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using Unity.VisualScripting;

public class ManagerGame : MonoBehaviour
{
    [Header("Pontos e Tempo")]
    [SerializeField]
    private Text          _textTime, _textPonto, _textFinal;
    [SerializeField]
    private float         _setTime;
    

    public static bool    _iniciou;
    public static int     _pontos;

    [Header("telas")]
    [SerializeField]
    private GameObject    _telaFinal;

    [Header("Anexos")]
    [SerializeField]
    private PlayerManager _pManager;
    [SerializeField]
    private Shooter _pShooter;
   
    

    void Start()
    {        
        Time.timeScale = 1;
        _iniciou = false;
        _setTime = menu._valorTime;
    }
    
    void Update()
    {        
        if (_iniciou)
        {         
             int temp = (int)_setTime;
            _setTime -= Time.deltaTime;
            _textTime.text= temp.ToString();          
        } 

        if(_pManager._morreu)
        {
            _telaFinal.SetActive(true);
            Time.timeScale = 0;
            _textFinal.text = "Derrota";
            _textPonto.text = _pontos.ToString();
        } 
        else if (_setTime <= 0)
        {
            _telaFinal.SetActive(true);
            Time.timeScale = 0;
            _textFinal.text = "Vitoria";
            _textPonto.text = _pontos.ToString();
        }
    }
    public void MenuPrincipal()
    {
        SceneManager.LoadScene("Menu");        
    }
    public void Reiniciar()
    {
        SceneManager.LoadScene(SceneManager.GetActiveScene().name);              
    }
}
