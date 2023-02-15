using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TiroManager : MonoBehaviour
{    
    [SerializeField]
    private float          _SpeedTiro;
    [SerializeField]
    private Animator       _anim;    
    public bool           _destoy;
    public float          _SpeedDestroyAuto;
    public int            _eixoX, _eixoY;

    void Start()
    {
       _anim = GetComponent<Animator>();
    }
    
    void Update()
    {
        _SpeedDestroyAuto += 1 * Time.deltaTime;
        if (_destoy || _SpeedDestroyAuto > 3)
        {
            Destroy(this.gameObject);            
        }
        moveTiro();
    }    

    void moveTiro()
    {
        Vector2 temp = new Vector2(_eixoX, _eixoY);
        transform.Translate((temp * Time.deltaTime * _SpeedTiro));        
    }

    private void OnCollisionEnter2D(Collision2D collision)
    {        
        _anim.SetBool("explosao", collision.collider.tag.Equals("Parede") || collision.collider.tag.Equals("Enemy"));              
        _SpeedTiro = 0;        
    }
}
